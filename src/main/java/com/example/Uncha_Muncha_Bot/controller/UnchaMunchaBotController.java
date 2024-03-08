package com.example.Uncha_Muncha_Bot.controller;

import com.example.Uncha_Muncha_Bot.constants.*;
import com.example.Uncha_Muncha_Bot.dto.AdvertisingDTO;
import com.example.Uncha_Muncha_Bot.dto.MediaDTO;
import com.example.Uncha_Muncha_Bot.dto.PharmacyDTO;
import com.example.Uncha_Muncha_Bot.dto.ProfileDTO;
import com.example.Uncha_Muncha_Bot.enums.*;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsAdmin;
import com.example.Uncha_Muncha_Bot.markUps.MarkUps;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsSuperAdmin;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsUser;
import com.example.Uncha_Muncha_Bot.service.*;
import io.github.nazarovctrl.telegrambotspring.bot.MessageSender;
import io.github.nazarovctrl.telegrambotspring.controller.AbstractUpdateController;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class UnchaMunchaBotController extends AbstractUpdateController {
    @Autowired
    private MessageSender messageSender;

    //===============MarksUps=================
    @Autowired
    private MarkUps markUps;
    @Autowired
    private MarkUpsSuperAdmin markUpsSuperAdmin;
    @Autowired
    private MarkUpsAdmin markUpsAdmin;
    @Autowired
    private MarkUpsUser markUpsUser;

    //=====================Service================
    @Autowired
    private ResourceBundleService resourceBundleService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private AdvertisingService advertisingService;
    @Autowired
    private MediaService mediaService;
    @Autowired
    private PharmacyService pharmacyService;

    @Override
    public void handle(Update update) {
        if (update.hasMessage()) {
            ProfileDTO currentProfile = profileService.getByChatId(update.getMessage().getChatId().toString());
            /**For checking status (Block!)*/
            if (currentProfile != null && currentProfile.getActiveStatus() != null) {
                if (currentProfile.getActiveStatus().equals(ActiveStatus.BLOCK)) {
                    if (currentProfile.getLanguage() != null) {
                        executeMessage(new SendMessage(currentProfile.getChatId(), resourceBundleService.getMessage("you.are.blocked", currentProfile.getLanguage())));
                    } else {
                        executeMessage(new SendMessage(currentProfile.getChatId(), resourceBundleService.getMessage("you.are.blocked", Language.uz)));
                    }
                    return;
                }
            }

            /** Update Username*/
            if (currentProfile != null && update.getMessage().getFrom().getUserName() != null) {
                String userName = update.getMessage().getFrom().getUserName();
                if (!(currentProfile.getUsername() != null && currentProfile.getUsername().equals(userName))) {
                    updateUsername(update.getMessage().getChatId().toString(), userName);
                }
            }
            /**For checking (/start,/help, ...) commands*/
            if (currentProfile != null && currentProfile.getPhone() != null) {
                if (checkCommand(update, currentProfile)) {
                    return;
                }
            }
            try {
                if (currentProfile != null && !currentProfile.getRole().equals(ProfileRole.USER)) {
                    if (currentProfile.getRole().equals(ProfileRole.SUPER_ADMIN)) {
                        messageSuperAdmin(update, currentProfile);
                    } else if (currentProfile.getRole().equals(ProfileRole.ADMIN)) {
                        messageAdmin(update, currentProfile);
                    }
                } else {
                    messageUser(update, currentProfile);
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
                messageUser(update, currentProfile);
            }
        } else if (update.hasCallbackQuery()) {
            ProfileDTO currentProfile = profileService.getByChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            /**For checking status (Block!)*/
            if (currentProfile != null && currentProfile.getActiveStatus() != null) {
                if (currentProfile.getActiveStatus().equals(ActiveStatus.BLOCK)) {
                    if (currentProfile.getLanguage() != null) {
                        executeMessage(new SendMessage(currentProfile.getChatId(), resourceBundleService.getMessage("you.are.blocked", currentProfile.getLanguage())));
                    } else {
                        executeMessage(new SendMessage(currentProfile.getChatId(), resourceBundleService.getMessage("you.are.blocked", Language.uz)));
                    }
                    return;
                }
            }

            /** Update Username*/
            if (currentProfile != null && update.getCallbackQuery().getFrom().getUserName() != null) {
                String userName = update.getCallbackQuery().getFrom().getUserName();
                if (!(currentProfile.getUsername() != null && currentProfile.getUsername().equals(userName))) {
                    updateUsername(update.getCallbackQuery().getMessage().getChatId().toString(), userName);
                }
            }
            assert currentProfile != null;
            if (currentProfile.getRole().equals(ProfileRole.SUPER_ADMIN)) {
                callBQSuperAdmin(update, currentProfile);
            } else if (currentProfile.getRole().equals(ProfileRole.ADMIN)) {
                callBQAdmin(update, currentProfile);
            } else {
                callBQUser(update, currentProfile);
            }
            return;
        }
    }

    // ===================================== USER ======================

    /**
     * For checking input message from User and return response
     */
    private void messageUser(Update update, ProfileDTO currentProfile) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        if (currentProfile == null) {
            ProfileDTO profile = new ProfileDTO();
            profile.setChatId(chatId);
            profile.setUsername("@" + update.getMessage().getChat().getUserName());
            profile.setRole(ProfileRole.USER);
            profile.setActiveStatus(ActiveStatus.BLOCK);
            profile.setCurrentStep(CommonConstants.LANGUAGE);
            profile.setCreatedDateTime(LocalDateTime.now());
            if (profileService.save(profile) != null) {
                log.info("New profile created username :: " + profile.getUsername());

                SendMessage sendMessage1 = new SendMessage();
                String langUz = resourceBundleService.getMessage("choosing.language", Language.uz);
                String langTr = resourceBundleService.getMessage("choosing.language", Language.tr);
                String langRu = resourceBundleService.getMessage("choosing.language", Language.ru);
                String langEn = resourceBundleService.getMessage("choosing.language", Language.en);
                sendMessage1.setText(langUz + "\n" + langTr + "\n" + langRu + "\n" + langEn);
                sendMessage1.setChatId(chatId);
                sendMessage1.setReplyMarkup(markUps.language());
                executeMessage(sendMessage1);
                return;
            }
        } else if (currentProfile.getPhone() == null) {
            Language language = currentProfile.getLanguage();
            if (message.hasContact()) {
                Contact contact = message.getContact();
                ProfileDTO profile = new ProfileDTO();
                profile.setName(contact.getFirstName());
                profile.setSurname(contact.getLastName());
                profile.setPhone(contact.getPhoneNumber());
                profile.setActiveStatus(ActiveStatus.ACTIVE);
                if (profileService.saveContact(profile, chatId) != null) {
                    log.info("Profile`s contact saved: username :: " + profile.getUsername() + " name :: " + profile.getName());
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText(resourceBundleService.getMessage("successful.registered", language));
                    sendMessage.setChatId(chatId);
                    ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
                    keyboardRemove.setRemoveKeyboard(true);
                    sendMessage.setReplyMarkup(keyboardRemove);
                    executeMessage(sendMessage);
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("menu", language));
                    sendMessage1.setReplyMarkup(markUpsUser.menu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, CommonConstants.MENU);
                    return;
                }
            } else if (language != null) {
                sendMessageAboutInvalidInput(language, chatId);
            }
        }
    }

    /**
     * For checking input callbackQuery from User and return response
     */
    private void callBQUser(Update update, ProfileDTO currentProfile) {
        CallbackQuery query = update.getCallbackQuery();
        String chatId = currentProfile.getChatId();

        if (currentProfile.getCurrentStep().equals(CommonConstants.LANGUAGE)) {
            if (currentProfile.getLanguage() == null) {
                if (query.getMessage().getText().contains(resourceBundleService.getMessage("choosing.language", Language.uz))
                        && query.getMessage().getText().contains(resourceBundleService.getMessage("choosing.language", Language.tr))) {
                    Language profileLanguage = Language.valueOf(query.getData());
                    profileService.changeLanguage(chatId, profileLanguage);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setReplyMarkup(markUps.contactButton(profileLanguage));
                    sendMessage.setText(resourceBundleService.getMessage("please.click.the.send.phone.number.button", profileLanguage));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, CommonConstants.SAND_CONTACT);
                    return;
                } else {
                    sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
                }

            } else {
                if (query.getMessage().getText().contains(resourceBundleService.getMessage("choosing.language", currentProfile.getLanguage()))) {
                    Language language = Language.valueOf(query.getData());
                    profileService.changeLanguage(chatId, Language.valueOf(query.getData()));
                    profileService.changeStep(chatId, CommonConstants.MENU);
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("menu", language));
                    sendMessage.setReplyMarkup(markUpsUser.menu(language));
                    executeMessage(sendMessage);
                } else {
                    sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
                }
            }
        }
    }


    // ===================================== ADMIN ======================

    /**
     * For checking input message from Admin and return response
     */
    private void messageAdmin(Update update, ProfileDTO currentProfile) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        if (currentProfile == null || currentProfile.getPhone() == null || currentProfile.getActiveStatus().equals(ActiveStatus.BLOCK)) {
            sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
            return;
        }
        Language language = currentProfile.getLanguage();
        String currentStep = currentProfile.getCurrentStep();
        if (message.hasLocation()) {
            Location location = message.getLocation();
            if (currentStep.equals(PharmacyConstants.ENTERING_PHARMACY_LOCATION)) {
                pharmacyService.setLocation(location, currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.finish.creating", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, PharmacyConstants.ACCEPT_TO_FINISH_CREATING);
                return;
            }
        }
        if (message.hasPhoto() || message.hasVideo()) {
            if (currentStep.equals(PharmacyConstants.ADD_MEDIA)) {
                List<PhotoSize> photo = message.getPhoto();
                String fileId = photo.get(photo.size() - 1).getFileId();
                MediaDTO media = new MediaDTO();
                media.setFId(fileId);
                media.setOwnerId(currentProfile.getChangingElementId());
                if (message.hasPhoto()) {
                    media.setMediaType(MediaType.PHOTO);
                } else {
                    media.setMediaType(MediaType.VIDEO);
                }
                mediaService.save(media);
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("media.saved", language)));
            }

            return;
        }
        if (message.hasText()) {
            String text = message.getText();
            if (currentStep.equals(PharmacyConstants.ENTERING_OWNER_USERNAME)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.pharmacy.working.end.time", language));
                    sendMessage.setReplyMarkup(markUps.time());
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, PharmacyConstants.CHOOSE_PHARMACY_WORKING_END_TIME);
                    return;
                }
                pharmacyService.setUsername(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.pharmacy.phone.number", language)));
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_PHARMACY_PHONE_NUMBER);
            } else if (currentStep.equals(PharmacyConstants.ENTERING_PHARMACY_PHONE_NUMBER)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.owner.username", language)));
                    profileService.changeStep(chatId, PharmacyConstants.ENTERING_OWNER_USERNAME);
                    return;
                }
                pharmacyService.setPharmacyPhone(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.pharmacy.name", language)));
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_PHARMACY_NAME);
            } else if (currentStep.equals(PharmacyConstants.ENTERING_PHARMACY_NAME)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.pharmacy.phone.number", language)));
                    profileService.changeStep(chatId, PharmacyConstants.ENTERING_PHARMACY_PHONE_NUMBER);
                    return;
                }
                pharmacyService.setPharmacyName(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.uz.info.about.pharmacy", language)));
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_UZ);
            } else if (currentStep.equals(PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_UZ)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.pharmacy.name", language)));
                    profileService.changeStep(chatId, PharmacyConstants.ENTERING_PHARMACY_NAME);
                    return;
                }
                pharmacyService.setUzInfo(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.tr.info.about.pharmacy", language)));
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_TR);
            } else if (currentStep.equals(PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_TR)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.uz.info.about.pharmacy", language)));
                    profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_UZ);
                    return;
                }
                pharmacyService.setTrInfo(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.ru.info.about.pharmacy", language)));
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_RU);
            } else if (currentStep.equals(PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_RU)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.tr.info.about.pharmacy", language)));
                    profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_TR);
                    return;
                }
                pharmacyService.setRuInfo(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.en.info.about.pharmacy", language)));
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_EN);
            } else if (currentStep.equals(PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_EN)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.ru.info.about.pharmacy", language)));
                    profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_RU);
                    return;
                }
                pharmacyService.setEnInfo(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.pharmacy.location", language)));
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_PHARMACY_LOCATION);
            } else if (currentStep.equals(PharmacyConstants.ENTERING_PHARMACY_LOCATION)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.en.info.about.pharmacy", language)));
                    profileService.changeStep(chatId, PharmacyConstants.ENTERING_INFO_ABOUT_PHARMACY_EN);
                    return;
                }
                sendMessageAboutInvalidInput(language, chatId);
            } else if (currentStep.equals(PharmacyConstants.SENDING_PHARMACY_ID_FOR_ADD_MEDIA)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("cancel.successfully", language));
                    sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                    executeMessage(sendMessage);
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                    return;
                }
                try {
                    Long pharmacyId = Long.valueOf(text);
                    PharmacyDTO pharmacyDTO = pharmacyService.findById(pharmacyId);
                    if (pharmacyDTO == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                        return;
                    } else {
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("entering.media", language));
                        sendMessage.setReplyMarkup(markUps.getNextAndBackButtons(language));
                        executeMessage(sendMessage);
                    }
                    profileService.changeChangingElementId(chatId, pharmacyId);
                    profileService.changeStep(chatId, PharmacyConstants.ADD_MEDIA);

                } catch (Exception e) {
                    sendMessageAboutInvalidInput(language, chatId);
                }
            } else if (currentStep.equals(PharmacyConstants.ADD_MEDIA)) {
                if (text.equals(resourceBundleService.getMessage("back", language))) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("sending.pharmacy.id", language));
                    sendMessage.setReplyMarkup(markUps.getBackButton(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, PharmacyConstants.SENDING_PHARMACY_ID_FOR_ADD_MEDIA);
                } else if (text.equals(resourceBundleService.getMessage("next", language))) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("media.saved", language));
                    sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                    executeMessage(sendMessage);
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                } else {
                    sendMessageAboutInvalidInput(language, chatId);
                }
            } else if (currentStep.equals(PharmacyConstants.SENDING_PHARMACY_ID_FOR_MAKE_BLOCK)) {
                try {
                    Long pharmacyId = Long.valueOf(text);
                    PharmacyDTO pharmacyDTO = pharmacyService.findById(pharmacyId);
                    if (pharmacyDTO == null || pharmacyDTO.getLatitude() == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                    } else {
                        pharmacyService.changeStatus(ActiveStatus.BLOCK, currentProfile.getChangingElementId());
                        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("status.changed", language)));
                    }
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                } catch (Exception e) {
                    sendMessageAboutInvalidInput(language, chatId);
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                }
            } else if (currentStep.equals(PharmacyConstants.SENDING_PHARMACY_ID_FOR_MAKE_UNBLOCK)) {
                try {
                    Long pharmacyId = Long.valueOf(text);
                    PharmacyDTO pharmacyDTO = pharmacyService.findById(pharmacyId);
                    if (pharmacyDTO == null || pharmacyDTO.getLatitude() == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                    } else {
                        pharmacyService.changeStatus(ActiveStatus.ACTIVE, currentProfile.getChangingElementId());
                        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("status.changed", language)));
                    }
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                } catch (Exception e) {
                    sendMessageAboutInvalidInput(language, chatId);
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                }
            } else if (currentStep.equals(PharmacyConstants.SENDING_PHARMACY_ID_FOR_GET)) {
                try {
                    Long pharmacyId = Long.valueOf(text);
                    PharmacyDTO pharmacyDTO = pharmacyService.findById(pharmacyId);
                    if (pharmacyDTO == null || pharmacyDTO.getLatitude() == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                    } else {
                        sendPharmacyList(message, language, chatId, List.of(pharmacyDTO));
                        profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                        return;
                    }
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                } catch (Exception e) {
                    sendMessageAboutInvalidInput(language, chatId);
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
                }
            }
        }


    }

    /**
     * For checking input callbackQuery from Admin and return response
     */
    private void callBQAdmin(Update update, ProfileDTO currentProfile) {
        CallbackQuery query = update.getCallbackQuery();
        String data = query.getData();
        String chatId = query.getMessage().getChatId().toString();
        String currentStep = currentProfile.getCurrentStep();
        Language language = currentProfile.getLanguage();

        List<String> languages = List.of("uz", "tr", "ru", "en");
        if (currentStep.equals(CommonConstants.LANGUAGE)) {
            if (languages.contains(data)) {
                Language language1 = Language.valueOf(data);
                profileService.changeLanguage(chatId, Language.valueOf(data));
                profileService.changeStep(chatId, CommonConstants.MENU);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup((InlineKeyboardMarkup) markUpsAdmin.menu(language1));
                executeEditMessage(editMessageText);
            } else {
                sendMessageAboutInvalidInput(language, chatId);

            }
        } else if (currentStep.equals(CommonConstants.MENU)) {
            if (data.equals(PharmacyConstants.PHARMACY)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("pharmacy.menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
            } else if (data.equals(HospitalConstants.HOSPITAL)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("hospital.menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
            } else if (data.equals(AutoConstants.AUTO)) {
                //todo

                profileService.changeStep(chatId, AutoConstants.AUTO);
            } else if (data.equals(HouseConstants.HOUSE)) {
                //todo

                profileService.changeStep(chatId, HouseConstants.HOUSE);
            } else if (data.equals(ShopConstants.SHOP)) {
                //todo

                profileService.changeStep(chatId, ShopConstants.SHOP);
            }
        } else if (currentStep.equals(PharmacyConstants.PHARMACY)) {
            if (data.equals(CommonConstants.BACK)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup((InlineKeyboardMarkup) markUpsAdmin.menu(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, CommonConstants.MENU);
            } else if (data.equals(PharmacyConstants.CREATE)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("accept.to.create", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUps.getAccept(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.ACCEPT_TO_CREATE);
            } else if (data.equals(PharmacyConstants.ADD_MEDIA)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("sending.pharmacy.id", language));
                sendMessage.setReplyMarkup(markUps.getBackButton(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, PharmacyConstants.SENDING_PHARMACY_ID_FOR_ADD_MEDIA);
            } else if (data.equals(PharmacyConstants.MAKE_BLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("sending.pharmacy.id", language)));
                profileService.changeStep(chatId, PharmacyConstants.SENDING_PHARMACY_ID_FOR_MAKE_BLOCK);
            } else if (data.equals(PharmacyConstants.MAKE_UNBLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("sending.pharmacy.id", language)));
                profileService.changeStep(chatId, PharmacyConstants.SENDING_PHARMACY_ID_FOR_MAKE_UNBLOCK);
            } else if (data.equals(PharmacyConstants.GET_ALL)) {
                sendPharmacyList(query.getMessage(), language, chatId, null);
            } else if (data.equals(PharmacyConstants.GET_BY_ID)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("sending.pharmacy.id", language)));
                profileService.changeStep(chatId, PharmacyConstants.SENDING_PHARMACY_ID_FOR_GET);
            }
        } else if (currentStep.equals(PharmacyConstants.ACCEPT_TO_CREATE)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("choose.pharmacy.type", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUpsAdmin.pharmacyType(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.CHOOSE_PHARMACY_TYPE);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("pharmacy.menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
            }
        } else if (currentStep.equals(PharmacyConstants.CHOOSE_PHARMACY_TYPE)) {
            if (data.equals(CommonConstants.BACK)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("accept.to.create", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUps.getAccept(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.ACCEPT_TO_CREATE);
            } else {
                PharmacyDTO pharmacy = new PharmacyDTO();
                if (data.equals(PharmacyConstants.PHARMACY_FOR_PEOPLE)) {
                    pharmacy.setPharmacyType(PharmacyType.PHARMACY);
                } else if (data.equals(PharmacyConstants.PHARMACY_FOR_ANIMALS)) {
                    pharmacy.setPharmacyType(PharmacyType.VET_PHARMACY);
                } else {
                    sendMessageAboutInvalidInput(language, chatId);
                    return;
                }
                pharmacy.setOwnerChatId(chatId);
                Long pharmacyId = pharmacyService.save(pharmacy);
                profileService.changeChangingElementId(chatId, pharmacyId);

                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("pharmacy.id", language) + " " + pharmacyId);
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                executeEditMessage(editMessageText);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.pharmacy.working.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, PharmacyConstants.CHOOSE_PHARMACY_WORKING_START_TIME);
            }
        } else if (currentStep.equals(PharmacyConstants.CHOOSE_PHARMACY_WORKING_START_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("choose.pharmacy.type", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUpsAdmin.pharmacyType(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.CHOOSE_PHARMACY_TYPE);
            } else if (data.endsWith(":00")) {
                pharmacyService.setStartTime(LocalTime.parse(data), currentProfile.getChangingElementId());
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("choose.pharmacy.working.end.time", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUps.time());
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.CHOOSE_PHARMACY_WORKING_END_TIME);
            } else {
                sendMessageAboutInvalidInput(language, chatId);
            }
        } else if (currentStep.equals(PharmacyConstants.CHOOSE_PHARMACY_WORKING_END_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("choose.pharmacy.working.start.time", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUps.time());
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.CHOOSE_PHARMACY_WORKING_START_TIME);
            } else if (data.endsWith(":00")) {
                pharmacyService.setEndTime(LocalTime.parse(data), currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("entering.owner.username", language));
                sendMessage.setReplyMarkup(markUps.getBackButton(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_OWNER_USERNAME);
            } else {
                sendMessageAboutInvalidInput(language, chatId);
            }
        } else if (currentStep.equals(PharmacyConstants.ACCEPT_TO_FINISH_CREATING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("creating.finished", language));
                sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                executeMessage(sendMessage);
                SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                executeMessage(sendMessage1);
                pharmacyService.markAsDone(currentProfile.getChangingElementId());
                profileService.changeStep(chatId, PharmacyConstants.CHOOSE_PHARMACY_TYPE);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("entering.pharmacy.location", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_PHARMACY_LOCATION);
            }
        } else if (currentStep.equals(HospitalConstants.HOSPITAL)) {
            if (data.equals(CommonConstants.BACK)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup((InlineKeyboardMarkup) markUpsAdmin.menu(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, CommonConstants.MENU);
            } else if (data.equals(PharmacyConstants.CREATE)) {

            } else if (data.equals(PharmacyConstants.ADD_MEDIA)) {

            } else if (data.equals(PharmacyConstants.MAKE_BLOCK)) {

            } else if (data.equals(PharmacyConstants.MAKE_UNBLOCK)) {

            } else if (data.equals(PharmacyConstants.GET_ALL)) {

            } else if (data.equals(PharmacyConstants.GET_BY_ID)) {

            }
        }
    }

    private void sendPharmacyList(Message message, Language language, String chatId, List<PharmacyDTO> pharmacyList) {
        if (pharmacyList == null) {
            pharmacyList = pharmacyService.getAll();
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet autoSheet = workbook.createSheet("pharmacy");

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillPattern(FillPatternType.DIAMONDS);
        cellStyle.setFillForegroundColor(IndexedColors.AQUA.index);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFamily(FontFamily.ROMAN);
        cellStyle.setFont(font);

        XSSFRow row1 = autoSheet.createRow(0);

        XSSFCell cellId = row1.createCell(0);
        cellId.setCellStyle(cellStyle);
        cellId.setCellValue("Id");

        XSSFCell cellPharmacyType = row1.createCell(1);
        cellPharmacyType.setCellStyle(cellStyle);
        cellPharmacyType.setCellValue("pharmacyType");

        XSSFCell cellStartTime = row1.createCell(2);
        cellStartTime.setCellStyle(cellStyle);
        cellStartTime.setCellValue("startTime");

        XSSFCell cellEndTime = row1.createCell(3);
        cellEndTime.setCellStyle(cellStyle);
        cellEndTime.setCellValue("endTime");

        XSSFCell cellUsername = row1.createCell(4);
        cellUsername.setCellStyle(cellStyle);
        cellUsername.setCellValue("username");

        XSSFCell cellPhone = row1.createCell(5);
        cellPhone.setCellStyle(cellStyle);
        cellPhone.setCellValue("phone");

        XSSFCell cellPharmacyName = row1.createCell(6);
        cellPharmacyName.setCellStyle(cellStyle);
        cellPharmacyName.setCellValue("pharmacyName");

        XSSFCell cellInfoUz = row1.createCell(7);
        cellInfoUz.setCellStyle(cellStyle);
        cellInfoUz.setCellValue("infoUz");

        XSSFCell cellInfoTr = row1.createCell(8);
        cellInfoTr.setCellStyle(cellStyle);
        cellInfoTr.setCellValue("infoTr");

        XSSFCell cellInfoRu = row1.createCell(9);
        cellInfoRu.setCellStyle(cellStyle);
        cellInfoRu.setCellValue("infoRu");

        XSSFCell cellInfoEn = row1.createCell(10);
        cellInfoEn.setCellStyle(cellStyle);
        cellInfoEn.setCellValue("infoEn");

        XSSFCell cellLatitude = row1.createCell(11);
        cellLatitude.setCellStyle(cellStyle);
        cellLatitude.setCellValue("latitude");

        XSSFCell cellLongitude = row1.createCell(12);
        cellLongitude.setCellStyle(cellStyle);
        cellLongitude.setCellValue("longitude");

        XSSFCell cellActiveStatus = row1.createCell(13);
        cellActiveStatus.setCellStyle(cellStyle);
        cellActiveStatus.setCellValue("activeStatus");

        XSSFCell cellCreatedDateTime = row1.createCell(14);
        cellCreatedDateTime.setCellStyle(cellStyle);
        cellCreatedDateTime.setCellValue("createdDateTime");

        XSSFCell cellOwnerChatId = row1.createCell(15);
        cellOwnerChatId.setCellStyle(cellStyle);
        cellOwnerChatId.setCellValue("ownerChatId");

        int i = 0;
        for (
                PharmacyDTO pharmacy : pharmacyList) {
            if (pharmacy.getLatitude() != null) {
                XSSFRow row = autoSheet.createRow(++i);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(pharmacy.getId());
                XSSFCell cell1 = row.createCell(1);
                cell1.setCellValue(pharmacy.getPharmacyType().name());
                XSSFCell cell2 = row.createCell(2);
                cell2.setCellValue(pharmacy.getStartTime().toString());
                XSSFCell cell3 = row.createCell(3);
                cell3.setCellValue(pharmacy.getEndTime().toString());
                XSSFCell cell4 = row.createCell(4);
                cell4.setCellValue(pharmacy.getUsername());
                XSSFCell cell5 = row.createCell(5);
                cell5.setCellValue(pharmacy.getPhone());
                XSSFCell cell6 = row.createCell(6);
                cell6.setCellValue(pharmacy.getPharmacyName());
                XSSFCell cell7 = row.createCell(7);
                cell7.setCellValue(pharmacy.getInfoUz());
                XSSFCell cell8 = row.createCell(8);
                cell8.setCellValue(pharmacy.getInfoTr());
                XSSFCell cell9 = row.createCell(9);
                cell9.setCellValue(pharmacy.getInfoRu());
                XSSFCell cell10 = row.createCell(10);
                cell10.setCellValue(pharmacy.getInfoEn());
                XSSFCell cell11 = row.createCell(11);
                cell11.setCellValue(pharmacy.getLatitude());
                XSSFCell cell12 = row.createCell(12);
                cell12.setCellValue(pharmacy.getLongitude());
                XSSFCell cell13 = row.createCell(13);
                cell13.setCellValue(pharmacy.getActiveStatus().name());
                XSSFCell cell14 = row.createCell(14);
                cell14.setCellValue(pharmacy.getCreatedDateTime().toString());
                XSSFCell cell15 = row.createCell(15);
                cell15.setCellValue(pharmacy.getOwnerChatId());
            }
        }
        try {
            workbook.write(new FileOutputStream("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\pharmacy.xlsx"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument = new SendDocument(chatId, new InputFile(new File("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\pharmacy.xlsx")));
        executeDocument(sendDocument);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
        sendMessage.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
        executeMessage(sendMessage);
    }

    // ===================================== SUPER_ADMIN ================

    /**
     * For checking input message from SuperAdmin and return response
     */
    private void messageSuperAdmin(Update update, ProfileDTO currentProfile) {
        Message message = update.getMessage();

        if (message.hasPhoto() || message.hasVideo() || message.hasLocation()) {
            if (currentProfile.getCurrentStep().equals(SuperAdminConstants.ENTERING_MEDIA_FOR_ADVERTISING)) {
                if (message.hasPhoto() || message.hasVideo()) {
                    List<PhotoSize> photo = message.getPhoto();
                    PhotoSize photoSize1 = photo.get(0);
                    for (PhotoSize photoSize : photo) {
                        if (photoSize.getFileId().length() > photoSize1.getFileId().length()) {
                            photoSize1 = photoSize;
                        }
                    }
                    String fileId = photoSize1.getFileId();
                    MediaDTO media = new MediaDTO();
                    media.setFId(fileId);
                    media.setOwnerId(currentProfile.getChangingElementId());
                    if (message.hasPhoto()) {
                        media.setMediaType(MediaType.PHOTO);
                    } else {
                        media.setMediaType(MediaType.VIDEO);
                    }
                    mediaService.save(media);
                    executeMessage(new SendMessage(currentProfile.getChatId(), resourceBundleService.getMessage("media.saved", currentProfile.getLanguage())));
                }
            }
            return;
        }

        String text = message.getText();
        String chatId = message.getChatId().toString();
        Language language = currentProfile.getLanguage();
        String currentStep = currentProfile.getCurrentStep();
        if (currentStep.equals(SuperAdminConstants.GETTING_BY_CHAT_ID)) {
            try {
                Long profileChatId = Long.valueOf(text);
                ProfileDTO profileDTO = profileService.getByChatId(profileChatId.toString());
                if (profileDTO != null) {
                    executeUserList(chatId, language, message, List.of(profileDTO), "profileByChatId");
                } else {
                    executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("user.not.found", language)));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                }
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
            } catch (Exception e) {
                log.warn(e.getMessage());
                sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
            }
        } else if (currentStep.equals(SuperAdminConstants.ENTERING_ID_FOR_MAKE_ADMIN)) {
            try {
                Long profileChatId = Long.valueOf(text);
                ProfileDTO profileDTO = profileService.getByChatId(profileChatId.toString());
                if (profileDTO != null) {
                    if (text.equals(chatId)) {
                        return;
                    }
                    profileService.changeRole(text, ProfileRole.ADMIN);
                    StringBuilder info = new StringBuilder();
                    if (currentProfile.getUsername() != null) {
                        info.append("Username :: ").append(profileDTO.getUsername());
                    }
                    if (currentProfile.getName() != null) {
                        info.append("\nName :: ").append(profileDTO.getName());
                    }
                    if (currentProfile.getSurname() != null) {
                        info.append("\nSurname :: ").append(profileDTO.getSurname());
                    }
                    info.append("\nRole :: ADMIN ✅");
                    executeMessage(new SendMessage(chatId, info.toString()));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                } else {
                    executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("user.not.found", language)));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                }
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
            } catch (Exception e) {
                log.warn(e.getMessage());
                sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
            }
        } else if (currentStep.equals(SuperAdminConstants.ENTERING_ID_FOR_MAKE_USER)) {
            try {
                Long profileChatId = Long.valueOf(text);
                ProfileDTO profileDTO = profileService.getByChatId(profileChatId.toString());
                if (profileDTO != null) {
                    if (text.equals(chatId)) {
                        return;
                    }
                    profileService.changeRole(text, ProfileRole.USER);
                    StringBuilder info = new StringBuilder();
                    if (currentProfile.getUsername() != null) {
                        info.append("Username :: ").append(profileDTO.getUsername());
                    }
                    if (currentProfile.getName() != null) {
                        info.append("\nName :: ").append(profileDTO.getName());
                    }
                    if (currentProfile.getSurname() != null) {
                        info.append("\nSurname :: ").append(profileDTO.getSurname());
                    }
                    info.append("\nRole :: USER ✅");
                    executeMessage(new SendMessage(chatId, info.toString()));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                } else {
                    executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("user.not.found", language)));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                }
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
            } catch (Exception e) {
                log.warn(e.getMessage());
                sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
            }
        } else if (currentStep.equals(SuperAdminConstants.ENTERING_ID_FOR_MAKE_ACTIVE)) {
            try {
                Long profileChatId = Long.valueOf(text);
                ProfileDTO profileDTO = profileService.getByChatId(profileChatId.toString());
                if (profileDTO != null) {
                    if (text.equals(chatId)) {
                        return;
                    }
                    profileService.changeStatus(text, ActiveStatus.ACTIVE);
                    StringBuilder info = new StringBuilder();
                    if (currentProfile.getUsername() != null) {
                        info.append("Username :: ").append(profileDTO.getUsername());
                    }
                    if (currentProfile.getName() != null) {
                        info.append("\nName :: ").append(profileDTO.getName());
                    }
                    if (currentProfile.getSurname() != null) {
                        info.append("\nSurname :: ").append(profileDTO.getSurname());
                    }
                    info.append("\nStatus :: ACTIVE ✅");
                    executeMessage(new SendMessage(chatId, info.toString()));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                } else {
                    executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("user.not.found", language)));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                }
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
            } catch (Exception e) {
                log.warn(e.getMessage());
                sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
            }
        } else if (currentStep.equals(SuperAdminConstants.ENTERING_ID_FOR_MAKE_BLOCK)) {
            try {
                Long profileChatId = Long.valueOf(text);
                ProfileDTO profileDTO = profileService.getByChatId(profileChatId.toString());
                if (profileDTO != null) {
                    if (text.equals(chatId)) {
                        return;
                    }
                    profileService.changeStatus(text, ActiveStatus.BLOCK);
                    StringBuilder info = new StringBuilder();
                    if (currentProfile.getUsername() != null) {
                        info.append("Username :: ").append(profileDTO.getUsername());
                    }
                    if (currentProfile.getName() != null) {
                        info.append("\nName :: ").append(profileDTO.getName());
                    }
                    if (currentProfile.getSurname() != null) {
                        info.append("\nSurname :: ").append(profileDTO.getSurname());
                    }
                    info.append("\nStatus :: BLOCK ✅");
                    executeMessage(new SendMessage(chatId, info.toString()));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                } else {
                    executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("user.not.found", language)));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                }
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
            } catch (Exception e) {
                log.warn(e.getMessage());
                sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
            }
        } else if (currentStep.equals(SuperAdminConstants.ENTERING_TEXT_FOR_ADVERTISING)) {
            if (text.equals(resourceBundleService.getMessage("back", language))) {
                SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("cancel.successfully", currentProfile.getLanguage()));
                sendMessage1.setReplyMarkup(new ReplyKeyboardRemove(true));
                executeMessage(sendMessage1);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("confirmation", currentProfile.getLanguage()));
                sendMessage.setReplyMarkup(markUpsSuperAdmin.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, SuperAdminConstants.ACCEPTING_TO_CREATE_ADVERTISING);
                return;
            }
            Long advertisingId = advertisingService.create(chatId, text);
            profileService.changeChangingElementId(chatId, advertisingId);
            profileService.changeStep(chatId, SuperAdminConstants.ENTERING_MEDIA_FOR_ADVERTISING);
            SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("entering.media", language));
            sendMessage.setReplyMarkup(markUps.getNextAndBackButtons(language));
            executeMessage(sendMessage);
        } else if (currentStep.equals(SuperAdminConstants.ENTERING_MEDIA_FOR_ADVERTISING)) {
            if (text.equals(resourceBundleService.getMessage("back", language))) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_TEXT_FOR_ADVERTISING);
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.description", language)));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("or.come.back", language));
                sendMessage.setReplyMarkup(markUps.getBackButton(currentProfile.getLanguage()));
                executeMessage(sendMessage);
            } else if (text.equals(resourceBundleService.getMessage("next", language))) {
                checkToSandAdvertising(currentProfile);
                profileService.changeStep(chatId, SuperAdminConstants.ACCEPT_TO_SEND_ADVERTISING);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("confirmation", language));
                sendMessage.setReplyMarkup(markUpsSuperAdmin.getAccept(language));
                executeMessage(sendMessage);
            }
        } else if (currentStep.equals(SuperAdminConstants.ENTERING_F_ID_FOR_GET_MEDIA)) {
            MediaDTO media = mediaService.getByFId(text);
            if (media == null) {
                sendMessageAboutInvalidInput(language, chatId);
            } else if (media.getMediaType().equals(MediaType.PHOTO)) {
                SendPhoto sendPhoto = new SendPhoto(chatId, new InputFile(media.getFId()));
                sendPhoto.setCaption("Owner id :: " + media.getOwnerId());
                executePhoto(sendPhoto);
            } else if (media.getMediaType().equals(MediaType.VIDEO)) {
                SendVideo sendVideo = new SendVideo(chatId, new InputFile(media.getFId()));
                sendVideo.setCaption("Owner id :: " + media.getOwnerId());
                executeVideo(sendVideo);
            }
            profileService.changeStep(chatId, SuperAdminConstants.MENU);
            SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
            sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
            executeMessage(sendMessage);
        }
    }

    /**
     * Checking advertising before send
     */
    private void checkToSandAdvertising(ProfileDTO currentProfile) {
        Long advertisingId = currentProfile.getChangingElementId();
        AdvertisingDTO advertisingDTO = advertisingService.getById(advertisingId);
        String advertisingDTOText = advertisingDTO.getText();
        String chatId = currentProfile.getChatId();
        sendMedia(advertisingId, advertisingDTOText, chatId, currentProfile.getLanguage(), currentProfile);
    }

    /**
     * For checking input callbackQuery from SuperAdmin and return response
     */
    private void callBQSuperAdmin(Update update, ProfileDTO currentProfile) {
        CallbackQuery query = update.getCallbackQuery();
        Message message = query.getMessage();
        String data = query.getData();
        String chatId = query.getMessage().getChatId().toString();
        String currentStep = currentProfile.getCurrentStep();
        Language profileLanguage = currentProfile.getLanguage();

        List<String> languages = List.of("uz", "tr", "ru", "en");
        if (currentStep.equals(CommonConstants.LANGUAGE)) {
            if (languages.contains(data)) {
                Language language = Language.valueOf(data);
                profileService.changeLanguage(chatId, Language.valueOf(data));
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("super.admin.menu", language));
                sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                executeMessage(sendMessage);
            } else {
                sendMessageAboutInvalidInput(profileLanguage, chatId);

            }
        } else if (currentStep.equals(SuperAdminConstants.MENU)) {
            if (data.equals(SuperAdminConstants.MAKE_ADMIN)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_ADMIN);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.MAKE_USER)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_USER);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.MAKE_ACTIVE)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_ACTIVE);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.MAKE_BLOCK)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_BLOCK);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.GET_ALL)) {
                sendAllProfileList(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.GET_ALL_ADMIN)) {
                sendAllAdminList(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.GET_ALL_USER)) {
                sendAllUserList(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.GET_BY_CHAT_ID)) {
                sendUserByChatId(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.CREATE_ADVERTISING)) {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setText(resourceBundleService.getMessage("confirmation", currentProfile.getLanguage()));
                editMessageText.setChatId(chatId);
                editMessageText.setReplyMarkup(markUpsSuperAdmin.getAccept(currentProfile.getLanguage()));
                editMessageText.setMessageId(message.getMessageId());
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, SuperAdminConstants.ACCEPTING_TO_CREATE_ADVERTISING);
            } else if (data.equals(SuperAdminConstants.GET_BY_F_ID)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_F_ID_FOR_GET_MEDIA);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.f.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            }
        } else if (currentStep.equals(SuperAdminConstants.ACCEPTING_TO_CREATE_ADVERTISING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_TEXT_FOR_ADVERTISING);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.description", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("or.come.back", profileLanguage));
                sendMessage.setReplyMarkup(markUps.getBackButton(currentProfile.getLanguage()));
                executeMessage(sendMessage);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("super.admin.menu", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                editMessageText.setReplyMarkup((InlineKeyboardMarkup) markUpsSuperAdmin.menu(currentProfile.getLanguage()));
                executeEditMessage(editMessageText);
            }
        } else if (currentStep.equals(SuperAdminConstants.ACCEPT_TO_SEND_ADVERTISING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                profileService.changeStep(chatId, SuperAdminConstants.ASK_LANGUAGE_TO_SEND_ADVERTISING);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("choose.language.to.send", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                editMessageText.setReplyMarkup((InlineKeyboardMarkup) markUps.language());
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_MEDIA_FOR_ADVERTISING);
                mediaService.deleteByOwnerId(currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("entering.media", profileLanguage));
                sendMessage.setReplyMarkup(markUps.getNextAndBackButtons(profileLanguage));
                executeMessage(sendMessage);
            }
        } else if (currentStep.equals(SuperAdminConstants.ASK_LANGUAGE_TO_SEND_ADVERTISING)) {
            if (!("uz,ru,en,tr").contains(data)) {
                return;
            }
            Long advertisingId = currentProfile.getChangingElementId();
            AdvertisingDTO advertisingDTO = advertisingService.getById(advertisingId);
            String advertisingDTOText = advertisingDTO.getText();
            profileService.changeStep(chatId, SuperAdminConstants.MENU);
            int sharedCount = sendMedia(advertisingId, advertisingDTOText, null, Language.valueOf(data), currentProfile);
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(query.getMessage().getMessageId());
            SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("advertisement.has.been.sent", profileLanguage) + " (" + sharedCount + ")");
            sendMessage1.setReplyMarkup(new ReplyKeyboardRemove(true));
            executeMessage(sendMessage1);
            SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("super.admin.menu", profileLanguage));
            sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(currentProfile.getLanguage()));
            executeMessage(sendMessage);
        }
    }

    /**
     * For send all profiles info
     */
    private void sendAllProfileList(Message message, String chatId, Language language) {
        List<ProfileDTO> users = profileService.getAllByRole(List.of(ProfileRole.SUPER_ADMIN, ProfileRole.ADMIN, ProfileRole.USER), List.of(Language.uz, Language.en, Language.ru, Language.tr));
        executeUserList(chatId, language, message, users, "all");
    }

    /**
     * For send all admins info
     */
    private void sendAllAdminList(Message message, String chatId, Language language) {
        List<ProfileDTO> users = profileService.getAllByRole(List.of(ProfileRole.ADMIN), List.of(Language.uz, Language.en, Language.ru, Language.tr));
        executeUserList(chatId, language, message, users, "admins");
    }

    /**
     * For send all users info
     */
    private void sendAllUserList(Message message, String chatId, Language language) {
        List<ProfileDTO> users = profileService.getAllByRole(List.of(ProfileRole.USER), List.of(Language.uz, Language.en, Language.ru, Language.tr));
        executeUserList(chatId, language, message, users, "users");
    }

    /**
     * For send user info by chatId
     */
    private void sendUserByChatId(Message message, String chatId, Language language) {
        profileService.changeStep(chatId, SuperAdminConstants.GETTING_BY_CHAT_ID);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("send.profile.id", language)));
    }

    /**
     * For send given Profile List on Excel format
     */
    private void executeUserList(String chatId, Language language, Message message, List<ProfileDTO> users, String fileName) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet autoSheet = workbook.createSheet("user");

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillPattern(FillPatternType.DIAMONDS);
        cellStyle.setFillForegroundColor(IndexedColors.AQUA.index);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFamily(FontFamily.ROMAN);
        cellStyle.setFont(font);

        XSSFRow row1 = autoSheet.createRow(0);

        XSSFCell cellId = row1.createCell(0);
        cellId.setCellStyle(cellStyle);
        cellId.setCellValue("Id");

        XSSFCell cellChatId = row1.createCell(1);
        cellChatId.setCellStyle(cellStyle);
        cellChatId.setCellValue("ChatId");

        XSSFCell cellActiveStatus = row1.createCell(2);
        cellActiveStatus.setCellStyle(cellStyle);
        cellActiveStatus.setCellValue("activeStatus");

        XSSFCell cellPhone = row1.createCell(3);
        cellPhone.setCellStyle(cellStyle);
        cellPhone.setCellValue("phone");

        XSSFCell cellUsername = row1.createCell(4);
        cellUsername.setCellStyle(cellStyle);
        cellUsername.setCellValue("username");

        XSSFCell cellCreatedDateTime = row1.createCell(5);
        cellCreatedDateTime.setCellStyle(cellStyle);
        cellCreatedDateTime.setCellValue("createdDateTime");

        XSSFCell cellLatitude = row1.createCell(6);
        cellLatitude.setCellStyle(cellStyle);
        cellLatitude.setCellValue("latitude");

        XSSFCell cellLongitude = row1.createCell(7);
        cellLongitude.setCellStyle(cellStyle);
        cellLongitude.setCellValue("longitude");

        XSSFCell cellName = row1.createCell(8);
        cellName.setCellStyle(cellStyle);
        cellName.setCellValue("name");

        XSSFCell cellSurname = row1.createCell(9);
        cellSurname.setCellStyle(cellStyle);
        cellSurname.setCellValue("surname");

        XSSFCell cellRole = row1.createCell(10);
        cellRole.setCellStyle(cellStyle);
        cellRole.setCellValue("role");

        XSSFCell cellCurrentStep = row1.createCell(11);
        cellCurrentStep.setCellStyle(cellStyle);
        cellCurrentStep.setCellValue("currentStep");

        XSSFCell cellSelectedPurchaseType = row1.createCell(12);
        cellSelectedPurchaseType.setCellStyle(cellStyle);
        cellSelectedPurchaseType.setCellValue("selectedPurchaseType");

        XSSFCell cellChangingElementId = row1.createCell(12);
        cellChangingElementId.setCellStyle(cellStyle);
        cellChangingElementId.setCellValue("changingElementId");

        XSSFCell cellLanguage = row1.createCell(12);
        cellLanguage.setCellStyle(cellStyle);
        cellLanguage.setCellValue("language");

        int i = 0;
        for (
                ProfileDTO user : users) {
            XSSFRow row = autoSheet.createRow(++i);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(user.getId());
            XSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(user.getChatId());
            XSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(user.getActiveStatus().name());
            XSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(user.getPhone());
            XSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(user.getUsername());
            XSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(user.getCreatedDateTime());
            XSSFCell cell6 = row.createCell(6);
            if (user.getLatitude() != null) {
                cell6.setCellValue(user.getLatitude());
            }
            XSSFCell cell7 = row.createCell(7);
            if (user.getLongitude() != null) {
                cell7.setCellValue(user.getLongitude());
            }
            XSSFCell cell8 = row.createCell(8);
            if (user.getName() != null) {
                cell8.setCellValue(user.getName());
            }
            XSSFCell cell9 = row.createCell(9);
            if (user.getSurname() != null) {
                cell9.setCellValue(user.getSurname());
            }
            XSSFCell cell10 = row.createCell(10);
            cell10.setCellValue(user.getRole().name());
            XSSFCell cell11 = row.createCell(11);
            cell11.setCellValue(user.getCurrentStep());
            XSSFCell cell12 = row.createCell(12);
            if (user.getSelectedPurchaseType() != null) {
                cell12.setCellValue(user.getSelectedPurchaseType().name());
            }
            XSSFCell cell13 = row.createCell(13);
            if (user.getChangingElementId() != null) {
                cell13.setCellValue(user.getChangingElementId());
            }
            XSSFCell cell14 = row.createCell(14);
            if (user.getLanguage() != null) {
                cell14.setCellValue(user.getLanguage().name());
            }

        }
        try {
            workbook.write(new FileOutputStream("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\" + fileName + ".xlsx"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument = new SendDocument(chatId, new InputFile(new File("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\" + fileName + ".xlsx")));
        executeDocument(sendDocument);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
        sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
        executeMessage(sendMessage);
    }

    // ===================================== EXECUTE ===================

    /**
     * For execute SendMessage
     */
    private boolean executeMessage(SendMessage sendMessage) {
        try {
            messageSender.execute(sendMessage);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * For update message
     * Last {Hellaa}
     * <p>
     * New {Hello}
     */
    private boolean executeEditMessage(EditMessageText editMessageText) {
        try {
            messageSender.execute(editMessageText);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * For execute Document
     */
    private void executeDocument(SendDocument sendDocument) {
        try {
            messageSender.execute(sendDocument);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * For Delete Message
     */
    private void executeDeleteMessage(DeleteMessage deleteMessage) {
        try {
            messageSender.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * For execute SendPhoto
     */
    private void executePhoto(SendPhoto sendPhoto) {
        try {
            messageSender.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * For execute SendVideo
     */
    private void executeVideo(SendVideo sendVideo) {
        try {
            messageSender.execute(sendVideo);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * For execute SendMediaGroup
     */
    private void executeMediaGroup(SendMediaGroup sendMediaGroup) {
        try {
            messageSender.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    // ===================================== GENERAL ===================

    /**
     * For update profile username when username changed
     */
    private void updateUsername(String chatId, String userName) {
        profileService.updateUsername(chatId, "@" + userName);
    }

    /**
     * For checking (/start, /language, /help, /about_owners, /connection)
     */
    private boolean checkCommand(Update update, ProfileDTO currentProfile) {
        if (!(currentProfile != null && currentProfile.getPhone() != null)) {
            return false;
        }

        if (update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            if (text.equals("/start")) {
                switch (currentProfile.getRole()) {
                    case SUPER_ADMIN -> {
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId);
                        message.setText(resourceBundleService.getMessage("super.admin.menu", currentProfile.getLanguage()));
                        message.setReplyMarkup(markUpsSuperAdmin.menu(currentProfile.getLanguage()));
                        executeMessage(message);
                        profileService.changeStep(chatId, SuperAdminConstants.MENU);
                        break;
                    }
                    case ADMIN -> {
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId);
                        message.setText(resourceBundleService.getMessage("menu", currentProfile.getLanguage()));
                        message.setReplyMarkup(markUpsAdmin.menu(currentProfile.getLanguage()));
                        executeMessage(message);
                        profileService.changeStep(chatId, CommonConstants.MENU);
                        break;
                    }
                    case USER -> {
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId);
                        message.setText(resourceBundleService.getMessage("menu", currentProfile.getLanguage()));
                        message.setReplyMarkup(markUpsUser.menu(currentProfile.getLanguage()));
                        executeMessage(message);
                        profileService.changeStep(chatId, CommonConstants.MENU);
                        break;
                    }
                }
                return true;
            } else if (text.equals("/language")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(resourceBundleService.getMessage("choosing.language", currentProfile.getLanguage()));
                message.setReplyMarkup(markUps.language());
                executeMessage(message);
                profileService.changeStep(chatId, CommonConstants.LANGUAGE);
                return true;
            } else if (text.equals("/help")) {
                ProfileDTO superAdmin = profileService.getSuperAdmin();
                if (superAdmin == null) {
                    superAdmin = profileService.getByChatId("2035107903");
                }
                String helpText1 = resourceBundleService.getMessage("help.1", currentProfile.getLanguage());
                String helpText2 = resourceBundleService.getMessage("help.2", currentProfile.getLanguage());
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(helpText1 + superAdmin.getUsername() + " " + helpText2);
                executeMessage(message);
                return true;
            } else if (text.equals("/about_owners")) {
                Language language = currentProfile.getLanguage();
                StringBuilder info = new StringBuilder(resourceBundleService.getMessage("bot.info.1", language));

                /**The usernames of the owners are added here*/
                for (ProfileDTO dto : profileService.getOwnersList()) {
                    info.append("\n").append(dto.getUsername()).append(",");
                }

                /**Here the (,) after the last username is removed*/
                info = new StringBuilder(info.substring(0, info.length() - 1)).append("\n");
                info.append(resourceBundleService.getMessage("bot.info.2", language));

                /** The total number of users is added here */
                info.append(profileService.getCount());
                info.append(resourceBundleService.getMessage("bot.info.3", language));

                executeMessage(new SendMessage(chatId, info.toString()));
                return true;
            } else if (text.equals("/connection")) {
                StringBuilder builder = new StringBuilder(" ");
                List<ProfileDTO> adminList = profileService.getAdminList();
                for (ProfileDTO dto : adminList) {
                    builder.append("\n").append(dto.getUsername()).append(", ");
                }

                if (adminList.size() != 0) {
                    builder = new StringBuilder(builder.substring(0, builder.length() - 2));
                } else {
                    try {
                        builder.append(profileService.getByChatId("994001445").getUsername());
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
                executeMessage(new SendMessage(chatId, builder.toString()));
                return true;
            }
        }
        return false;
    }

    /**
     * For sand message (Entered invalid message)
     */
    private void sendMessageAboutInvalidInput(Language language, String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(resourceBundleService.getMessage("invalid.query.entered", language));
        sendMessage.setChatId(chatId);
        executeMessage(sendMessage);
    }

    /**
     * For sand media(ownerId = Announcement id)(text = Caption)
     *
     * @return
     */
    private int sendMedia(Long ownerId, String text, String chatId, Language language, ProfileDTO currentProfile) {
        List<MediaDTO> mediaList = mediaService.getByOwnerId(ownerId);
        List<ProfileDTO> profileDTOList = profileService.getAllByRole(List.of(ProfileRole.SUPER_ADMIN, ProfileRole.ADMIN, ProfileRole.USER), List.of(language));
        if (chatId != null) {
            profileDTOList = List.of(profileService.getByChatId(chatId));
        }
        if (mediaList.size() == 0) {
            for (ProfileDTO profile : profileDTOList) {
                executeMessage(new SendMessage(profile.getChatId(), text));
            }
            return 0;
        }
        for (ProfileDTO profile : profileDTOList) {
            int mediaSize = mediaList.size();
            int count = 0;
            List<InputMedia> inputMediaList = new LinkedList<>();
            for (MediaDTO media : mediaList) {
                if (mediaSize % 10 == 1 && count + 1 == mediaSize) {
                    break;
                }
                if (media.getMediaType().equals(MediaType.PHOTO)) {
                    InputMedia photo = new InputMediaPhoto();
                    photo.setMedia(media.getFId());
                    inputMediaList.add(photo);
                } else if (media.getMediaType().equals(MediaType.VIDEO)) {
                    InputMedia photo = new InputMediaVideo();
                    photo.setMedia(media.getFId());
                    inputMediaList.add(photo);
                }
                count++;
            }
            if (mediaSize > 1) {
                SendMediaGroup sendMediaGroup = new SendMediaGroup();
                sendMediaGroup.setMedias(inputMediaList);
                sendMediaGroup.setChatId(profile.getChatId());
                executeMediaGroup(sendMediaGroup);
            }
            if (count + 1 == mediaSize) {
                MediaDTO media = mediaList.get(mediaSize - 1);
                if (media.getMediaType().equals(MediaType.PHOTO)) {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setPhoto(new InputFile(media.getFId()));
                    sendPhoto.setChatId(profile.getChatId());
                    sendPhoto.setCaption(text);
                    executePhoto(sendPhoto);
                } else if (media.getMediaType().equals(MediaType.VIDEO)) {
                    SendVideo sendVideo = new SendVideo();
                    sendVideo.setCaption(text);
                    sendVideo.setChatId(profile.getChatId());
                    sendVideo.setVideo(new InputFile(media.getFId()));
                    executeVideo(sendVideo);
                }
            } else {
                executeMessage(new SendMessage(profile.getChatId(), text));
            }
        }
        advertisingService.setSharedCount(profileDTOList.size(), currentProfile.getChangingElementId());
        return profileDTOList.size();
    }

}

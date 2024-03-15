package com.example.Uncha_Muncha_Bot.controller;

import com.example.Uncha_Muncha_Bot.constants.*;
import com.example.Uncha_Muncha_Bot.dto.*;
import com.example.Uncha_Muncha_Bot.entity.HospitalServiceEntity;
import com.example.Uncha_Muncha_Bot.enums.*;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsAdmin;
import com.example.Uncha_Muncha_Bot.markUps.MarkUps;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsSuperAdmin;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsUser;
import com.example.Uncha_Muncha_Bot.service.*;
import com.example.Uncha_Muncha_Bot.service.HospitalService;
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
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalServicesService hospitalServicesService;
    @Autowired
    private AutoService autoService;
    @Autowired
    private AutoSalonService autoSalonService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private AutoServicesService autoServicesService;

    @Override
    public void handle(Update update) {
        if (update.hasMessage()) {
            ProfileDTO currentProfile = profileService.getByChatId(update.getMessage().getChatId().toString());
            /**For checking status (Block!)*/
            if ((currentProfile != null && currentProfile.getActiveStatus() != null)) {
                if (currentProfile.getActiveStatus().equals(ActiveStatus.BLOCK) && currentProfile.getPhone() != null) {
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
                if (currentProfile.getActiveStatus().equals(ActiveStatus.BLOCK) && currentProfile.getPhone() != null) {
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

                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.finish.creating.pharmacy", language));

                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, PharmacyConstants.ACCEPT_TO_FINISH_CREATING);
                return;
            } else if (currentStep.equals(HospitalConstants.ENTERING_HOSPITAL_LOCATION)) {
                hospitalService.setLocation(location, currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.finish.creating.hospital", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, HospitalConstants.ACCEPT_TO_FINISH_CREATING);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_LOCATION)) {
                autoService.setLocation(location, currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.finish.creating", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.ACCEPT_TO_FINISH_CREATING);
            } else if (currentStep.equals(AutoSalonConstants.ENTER_LOCATION)) {
                autoSalonService.setLocation(location, currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.finish.creating", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ACCEPT_TO_FINISH_CREATING);
            } else if (currentStep.equals(ShopConstants.ENTER_LOCATION)) {
                shopService.setLocation(location, currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.finish.creating", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.ACCEPT_TO_FINISH_CREATING);
            }
            return;
        }
        if (message.hasPhoto() || message.hasVideo()) {
            if (currentStep.equals(PharmacyConstants.ADD_MEDIA) ||
                    currentStep.equals(HospitalConstants.ENTERING_MEDIA_FOR_HOSPITAL) ||
                    currentStep.equals(AutoBoughtConstants.ENTER_MEDIA) ||
                    currentStep.equals(ShopConstants.ENTER_MEDIA)) {
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

                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.pharmacy.owner.username", language)));
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
            } else if (currentStep.equals(HospitalConstants.ENTERING_OWNER_USERNAME)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.hospital.working.end.time", language));
                    sendMessage.setReplyMarkup(markUps.time());
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, HospitalConstants.CHOOSE_HOSPITAL_WORKING_END_TIME);
                    return;
                }
                hospitalService.setUserName("@" + text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.phone", language)));
                profileService.changeStep(chatId, HospitalConstants.ENTERING_PHONE);
            } else if (currentStep.equals(HospitalConstants.ENTERING_PHONE)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.owner.username", language)));
                    profileService.changeStep(chatId, HospitalConstants.ENTERING_OWNER_USERNAME);
                    return;
                }
                hospitalService.setPhone(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.name", language)));
                profileService.changeStep(chatId, HospitalConstants.ENTERING_HOSPITAL_NAME);
            } else if (currentStep.equals(HospitalConstants.ENTERING_HOSPITAL_NAME)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.phone", language)));
                    profileService.changeStep(chatId, HospitalConstants.ENTERING_PHONE);
                    return;
                }
                hospitalService.setName(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.uz", language)));
                profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_UZ);
            } else if (currentStep.equals(HospitalConstants.ENTERING_INFO_UZ)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.name", language)));
                    profileService.changeStep(chatId, HospitalConstants.ENTERING_HOSPITAL_NAME);
                    return;
                }
                hospitalService.setInfoUz(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.tr", language)));
                profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_TR);
            } else if (currentStep.equals(HospitalConstants.ENTERING_INFO_TR)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.uz", language)));
                    profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_UZ);
                    return;
                }
                hospitalService.setInfoTr(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.ru", language)));
                profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_RU);
            } else if (currentStep.equals(HospitalConstants.ENTERING_INFO_RU)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.tr", language)));
                    profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_TR);
                    return;
                }
                hospitalService.setInfoRu(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.en", language)));
                profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_EN);
            } else if (currentStep.equals(HospitalConstants.ENTERING_INFO_EN)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.ru", language)));
                    profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_RU);
                    return;
                }
                hospitalService.setInfoEn(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.location", language)));
                profileService.changeStep(chatId, HospitalConstants.ENTERING_HOSPITAL_LOCATION);
            } else if (currentStep.equals(HospitalConstants.ENTERING_HOSPITAL_LOCATION)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.info.en", language)));
                    profileService.changeStep(chatId, HospitalConstants.ENTERING_INFO_EN);
                    return;
                }
                sendMessageAboutInvalidInput(language, chatId);
            } else if (currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_MEDIA) ||
                    currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_BLOCK) ||
                    currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_UNBLOCK) ||
                    currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_GET_HOSPITAL)) {
                try {
                    Long hospitalId = Long.parseLong(text);
                    HospitalDTO hospitalDTO = hospitalService.getById(hospitalId);
                    if (hospitalDTO == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
                        return;
                    }
                    if (currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_MEDIA)) {
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("sending.media", language));
                        sendMessage.setReplyMarkup(markUps.getNextAndBackButtons(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, HospitalConstants.ENTERING_MEDIA_FOR_HOSPITAL);
                    } else if (currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_BLOCK)) {
                        hospitalService.changeStatus(ActiveStatus.BLOCK, hospitalDTO.getId());
                        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("status.changed", language)));
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
                    } else if (currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_UNBLOCK)) {
                        hospitalService.changeStatus(ActiveStatus.ACTIVE, hospitalDTO.getId());
                        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("status.changed", language)));
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
//                    } else if (currentStep.equals(HospitalConstants.GET_HOSPITAL_ID_FOR_GET_HOSPITAL)) {
                    } else {
                        sendHospitalList(message, chatId, language, List.of(hospitalDTO));
                        profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                    sendMessageAboutInvalidInput(language, chatId);
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
                }
            } else if (currentStep.equals(HospitalConstants.ENTERING_MEDIA_FOR_HOSPITAL)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("media.saved", language));
                    sendMessage1.setReplyMarkup(new ReplyKeyboardRemove(true));
                    executeMessage(sendMessage1);
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                    executeMessage(sendMessage);
                    return;

                } else if (text.equals(resourceBundleService.getMessage("next", language))) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("media.saved", language));
                    sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                    executeMessage(sendMessage);

                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
                    sendMessage1.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
                } else {
                    sendMessageAboutInvalidInput(language, chatId);
                }

            } else if (currentStep.equals(AutoBoughtConstants.ENTER_CITY)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.end.time", language));
                    sendMessage.setReplyMarkup(markUps.time());
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_END_TIME);
                    return;
                }
                autoService.setCity(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.district", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_DISTRICT);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_DISTRICT)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.city", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_CITY);
                    return;
                }
                autoService.setDistrict(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.brand", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_BRAND_NAME);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_BRAND_NAME)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.district", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_DISTRICT);
                    return;
                }
                autoService.setBrandName(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.model", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_MODEL_NAME);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_MODEL_NAME)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.brand", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_BRAND_NAME);
                    return;
                }
                autoService.setModelName(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.price", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_PRICE);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_PRICE)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.model", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_MODEL_NAME);
                    return;
                }
                try {
                    Double price = Double.valueOf(GeneralService.checkMoneyFromTheString(text));
                    autoService.setPrice(String.valueOf(price), currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.phone", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_PHONE);
                } catch (Exception e) {
                    sendMessageAboutInvalidInput(language, chatId);
                }
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_PHONE)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.model", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_PRICE);
                    return;
                }
                autoService.setPhone(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.username", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_USERNAME);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_USERNAME)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.phone", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_PHONE);
                    return;
                }
                autoService.setUsername(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.uz", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_UZ);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_INFO_UZ)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.username", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_USERNAME);
                    return;
                }
                autoService.setInfoUz(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.tr", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_TR);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_INFO_TR)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.uz", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_UZ);
                    return;
                }
                autoService.setInfoTr(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.ru", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_RU);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_INFO_RU)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.tr", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_TR);
                    return;
                }
                autoService.setInfoRu(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.en", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_EN);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_INFO_EN)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.ru", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_RU);
                    return;
                }
                autoService.setInfoEn(text, currentProfile.getChangingElementId());
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.location", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_LOCATION);
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_LOCATION)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.en", language)));
                    profileService.changeStep(chatId, AutoBoughtConstants.ENTER_INFO_EN);
                    return;
                }
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_ADD_MEDIA) ||
                    currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_BLOCK) ||
                    currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_UNBLOCK) ||
                    currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_GET_AUTO)) {
                try {
                    Long autoId = Long.valueOf(text);
                    AutomobileDTO automobileDTO = autoService.getById(autoId);
                    if (automobileDTO == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
                        return;
                    }
                    if (currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_ADD_MEDIA)) {
                        profileService.changeChangingElementId(chatId, autoId);
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("sending.media", language));
                        sendMessage.setReplyMarkup(markUps.getNextAndBackButtons(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, AutoBoughtConstants.ENTER_MEDIA);
                        return;
                    } else if (currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_BLOCK)) {
                        autoService.changeStatus(ActiveStatus.BLOCK, autoId);
                    } else if (currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_UNBLOCK)) {
                        autoService.changeStatus(ActiveStatus.ACTIVE, autoId);
//                } else if (currentStep.equals(AutoBoughtConstants.ENTER_ID_TO_GET_AUTO)) {
                    } else {
                        sendAutoList(message, chatId, language, List.of(automobileDTO));
                        return;
                    }
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
                } catch (Exception e) {
                    sendMessageAboutInvalidInput(language, chatId);
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
                }
            } else if (currentStep.equals(AutoBoughtConstants.ENTER_MEDIA)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
                } else if (text.equals(CommonConstants.NEXT)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_ID_TO_ADD_MEDIA) ||
                    currentStep.equals(AutoSalonConstants.ENTER_ID_TO_BLOCK) ||
                    currentStep.equals(AutoSalonConstants.ENTER_ID_TO_UNBLOCK) ||
                    currentStep.equals(AutoSalonConstants.ENTER_ID_TO_GET_SALON)) {
                try {
                    Long autoId = Long.valueOf(text);
                    AutoSalonDTO autoSalonDTO = autoSalonService.getById(autoId);
                    if (autoSalonDTO == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, AutoSalonConstants.SALON);
                        return;
                    }
                    if (currentStep.equals(AutoSalonConstants.ENTER_ID_TO_ADD_MEDIA)) {
                        profileService.changeChangingElementId(chatId, autoId);
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("sending.media", language));
                        sendMessage.setReplyMarkup(markUps.getNextAndBackButtons(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, AutoBoughtConstants.ENTER_MEDIA);
                        return;
                    } else if (currentStep.equals(AutoSalonConstants.ENTER_ID_TO_BLOCK)) {
                        autoSalonService.changeStatus(ActiveStatus.BLOCK, autoId);
                    } else if (currentStep.equals(AutoSalonConstants.ENTER_ID_TO_UNBLOCK)) {
                        autoSalonService.changeStatus(ActiveStatus.ACTIVE, autoId);
//                } else if (currentStep.equals(AutoSalonConstants.ENTER_ID_TO_GET_SALON)) {
                    } else {
                        sendAutoSalonList(message, chatId, language, List.of(autoSalonDTO));
                        profileService.changeStep(chatId, AutoSalonConstants.SALON);
                        return;
                    }
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoSalonConstants.SALON);
                } catch (Exception e) {
                    sendMessageAboutInvalidInput(language, chatId);
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoSalonConstants.SALON);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_USERNAME)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.end.time", language));
                    sendMessage.setReplyMarkup(markUps.time());
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_END_TIME);
                } else {
                    autoSalonService.setUsername("@" + text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.phone", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_PHONE);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_PHONE)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.username", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_USERNAME);
                } else {
                    autoSalonService.setPhone(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.brand", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_BRAND);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_BRAND)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.phone", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_PHONE);
                } else {
                    autoSalonService.setBrand(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.uz", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_UZ);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_INFO_UZ)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.brand", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_BRAND);
                } else {
                    autoSalonService.setInfoUz(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.tr", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_TR);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_INFO_TR)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.uz", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_UZ);
                } else {
                    autoSalonService.setInfoTr(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.ru", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_RU);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_INFO_RU)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.tr", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_TR);
                } else {
                    autoSalonService.setInfoRu(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.en", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_EN);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_INFO_EN)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.ru", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_RU);
                } else {
                    autoSalonService.setInfoEn(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.location", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_LOCATION);
                }
            } else if (currentStep.equals(AutoSalonConstants.ENTER_LOCATION)) {
                if (text.equals(CommonConstants.BACK)) {
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.en", language)));
                    profileService.changeStep(chatId, AutoSalonConstants.ENTER_INFO_EN);
                } else {
                    sendMessageAboutInvalidInput(language, chatId);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_USERNAME)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.end.time", language));
                    sendMessage.setReplyMarkup(markUps.time());
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_END_TIME);
                } else {
                    shopService.setUsername("@" + text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.city", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_CITY);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_CITY)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.username", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_USERNAME);
                } else {
                    shopService.setCity(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.district", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_DISTRICT);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_DISTRICT)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.city", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_CITY);
                } else {
                    shopService.setDistrict(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.phone", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_PHONE);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_PHONE)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.district", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_DISTRICT);
                } else {
                    shopService.setPhone(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.brand", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_BRAND);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_BRAND)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.phone", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_PHONE);
                } else {
                    shopService.setBrand(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.uz", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_UZ);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_INFO_UZ)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.brand", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_BRAND);
                } else {
                    shopService.setInfoUz(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.tr", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_TR);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_INFO_TR)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.info.uz", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_UZ);
                } else {
                    shopService.setInfoTr(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.ru", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_RU);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_INFO_RU)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.info.tr", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_TR);
                } else {
                    shopService.setInfoRu(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.info.en", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_EN);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_INFO_EN)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.info.ru", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_RU);
                } else {
                    shopService.setInfoEn(text, currentProfile.getChangingElementId());
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.location", language)));
                    profileService.changeStep(chatId, ShopConstants.ENTER_LOCATION);
                }
            } else if (currentStep.equals(ShopConstants.ENTER_LOCATION)) {
                if (text.equals(CommonConstants.BACK)) {
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.info.en", language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.ENTER_INFO_EN);
                }
            } else if (currentStep.equals(ShopConstants.ADD_MEDIA) ||
                    currentStep.equals(ShopConstants.BLOCK) ||
                    currentStep.equals(ShopConstants.UNBLOCK) ||
                    currentStep.equals(ShopConstants.GET_BY_ID)) {
                try {
                    Long shopId = Long.valueOf(text);
                    ShopDTO shop = shopService.getById(shopId);
                    if (shop == null) {
                        sendMessageAboutInvalidInput(language, chatId);
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, ShopConstants.SHOP);
                    } else if (currentStep.equals(ShopConstants.ADD_MEDIA)) {
                        profileService.changeChangingElementId(chatId, shopId);
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("entering.media", language));
                        sendMessage.setReplyMarkup(markUps.getNextAndBackButtons(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, ShopConstants.ENTER_MEDIA);
                    } else if (currentStep.equals(ShopConstants.BLOCK)) {
                        shopService.changeStatus(ActiveStatus.BLOCK, shopId);
                        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("status.changed", language)));
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, ShopConstants.SHOP);
                    } else if (currentStep.equals(ShopConstants.UNBLOCK)) {
                        shopService.changeStatus(ActiveStatus.ACTIVE, shopId);
                        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("status.changed", language)));
                        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                        sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                        executeMessage(sendMessage);
                        profileService.changeStep(chatId, ShopConstants.SHOP);
//                    } else if (currentStep.equals(ShopConstants.GET_BY_ID)){
                    } else {
                        sendShopList(message, chatId, language, List.of(shop));
                        profileService.changeStep(chatId, ShopConstants.SHOP);
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            } else if (currentStep.equals(ShopConstants.ENTER_MEDIA)) {
                if (text.equals(CommonConstants.BACK) ||
                        text.equals(CommonConstants.NEXT)) {
                    SendMessage message1 = new SendMessage(chatId, resourceBundleService.getMessage("media.saved", language));
                    message1.setReplyMarkup(new ReplyKeyboardRemove(true));
                    executeMessage(message1);
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                    sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, ShopConstants.SHOP);
                } else {
                    sendMessageAboutInvalidInput(language, chatId);
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
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
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
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoConstants.AUTO);
            } else if (data.equals(HouseConstants.HOUSE)) {
                // todo

                profileService.changeStep(chatId, HouseConstants.HOUSE);
            } else if (data.equals(ShopConstants.SHOP)) {
                // todo
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.SHOP);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
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
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("accept.to.create.pharmacy", language));
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
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
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
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(PharmacyConstants.CHOOSE_PHARMACY_TYPE)) {
            if (data.equals(CommonConstants.BACK)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("accept.to.create.pharmacy", language));
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
                    executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
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
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
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

                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("entering.pharmacy.owner.username", language));
                sendMessage.setReplyMarkup(markUps.getBackButton(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_OWNER_USERNAME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(PharmacyConstants.ACCEPT_TO_FINISH_CREATING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("creating.finished.pharmacy", language));

                sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                executeMessage(sendMessage);
                SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("pharmacy.menu", language));
                sendMessage1.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                executeMessage(sendMessage1);
                pharmacyService.markAsDone(currentProfile.getChangingElementId());
                profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("entering.pharmacy.location", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.ENTERING_PHARMACY_LOCATION);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(HospitalConstants.HOSPITAL)) {
            if (data.equals(CommonConstants.BACK)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup((InlineKeyboardMarkup) markUpsAdmin.menu(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, CommonConstants.MENU);

            } else if (data.equals(HospitalConstants.CREATE)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("accept.to.create.hospital", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUps.getAccept(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, HospitalConstants.ACCEPT_TO_CREATE);
            } else if (data.equals(HospitalConstants.ADD_MEDIA)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.hospital.id", language)));
                profileService.changeStep(chatId, HospitalConstants.GET_HOSPITAL_ID_FOR_MEDIA);
            } else if (data.equals(HospitalConstants.MAKE_BLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.hospital.id", language)));
                profileService.changeStep(chatId, HospitalConstants.GET_HOSPITAL_ID_FOR_BLOCK);
            } else if (data.equals(HospitalConstants.MAKE_UNBLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.hospital.id", language)));
                profileService.changeStep(chatId, HospitalConstants.GET_HOSPITAL_ID_FOR_UNBLOCK);
            } else if (data.equals(HospitalConstants.GET_ALL)) {
                sendHospitalList(query.getMessage(), chatId, language, null);
                profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
            } else if (data.equals(HospitalConstants.GET_BY_ID)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.hospital.id", language)));
                profileService.changeStep(chatId, HospitalConstants.GET_HOSPITAL_ID_FOR_GET_HOSPITAL);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(HospitalConstants.ACCEPT_TO_CREATE)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                Long id = hospitalService.create(chatId);
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("added.new.hospital", language) + id));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.hospital.services", language) + ")");
                sendMessage.setReplyMarkup(markUpsAdmin.hospitalType(language));
                executeMessage(sendMessage);
                profileService.changeChangingElementId(chatId, id);
                profileService.changeStep(chatId, HospitalConstants.CHOOSE_HOSPITAL_SERVICES);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("pharmacy.menu", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUpsAdmin.pharmacyMenu(language));
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, PharmacyConstants.PHARMACY);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(HospitalConstants.CHOOSE_HOSPITAL_SERVICES)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.create.hospital", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, HospitalConstants.ACCEPT_TO_CREATE);
            } else if (data.equals(CommonConstants.NEXT)) {
                if (query.getMessage().getText().equals(resourceBundleService.getMessage("choose.hospital.services", language) + ")")) {
                    return;
                }
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.hospital.working.start.time", language) + ")");
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, HospitalConstants.CHOOSE_HOSPITAL_WORKING_START_TIME);
            } else if ("DENTIST,LOR,PEDIATRICIAN,CARDIOLOGIST,DERMATOLOGIST,NEUROLOGIST,OPHTHALMOLOGIST,ORTHOPEDIST,UROLOGIST,DIETICIAN,PSYCHOLOGIST,VET".contains(data)) {
                hospitalServicesService.checkService(data, currentProfile.getChangingElementId());
                StringBuilder hospitalServicesList = new StringBuilder(resourceBundleService.getMessage("choose.hospital.services", language));
                for (HospitalServiceEntity entity : hospitalServicesService.getByHospitalId(currentProfile.getChangingElementId())) {
                    hospitalServicesList.append(entity.getServiceName()).append(", ");
                }
                if (!hospitalServicesList.toString().equals(resourceBundleService.getMessage("choose.hospital.services", language))) {
                    hospitalServicesList = new StringBuilder(hospitalServicesList.substring(0, hospitalServicesList.length() - 2));
                }
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage message = new SendMessage(chatId, hospitalServicesList + ")");
                message.setReplyMarkup(markUpsAdmin.hospitalType(language));
                executeMessage(message);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(HospitalConstants.CHOOSE_HOSPITAL_WORKING_START_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                StringBuilder hospitalServicesList = new StringBuilder(resourceBundleService.getMessage("choose.hospital.services", language));
                for (HospitalServiceEntity entity : hospitalServicesService.getByHospitalId(currentProfile.getChangingElementId())) {
                    hospitalServicesList.append(entity.getServiceName()).append(", ");
                }
                if (!hospitalServicesList.toString().equals(resourceBundleService.getMessage("choose.hospital.services", language))) {
                    hospitalServicesList = new StringBuilder(hospitalServicesList.substring(0, hospitalServicesList.length() - 2));
                }
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage message = new SendMessage(chatId, hospitalServicesList + ")");
                message.setReplyMarkup(markUpsAdmin.hospitalType(language));
                executeMessage(message);
                profileService.changeStep(chatId, HospitalConstants.CHOOSE_HOSPITAL_SERVICES);
            } else if (data.endsWith(":00")) {
                hospitalService.setStartTime(LocalTime.parse(data), currentProfile.getChangingElementId());
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("choose.hospital.working.end.time", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setReplyMarkup(markUps.time());
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, HospitalConstants.CHOOSE_HOSPITAL_WORKING_END_TIME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(HospitalConstants.CHOOSE_HOSPITAL_WORKING_END_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.hospital.working.start.time", language) + ")");
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, HospitalConstants.CHOOSE_HOSPITAL_WORKING_START_TIME);
            } else if (data.endsWith(":00")) {
                hospitalService.setEndTime(LocalTime.parse(data), currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("entering.hospital.owner.username", language));
                sendMessage.setReplyMarkup(markUps.getBackButton(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, HospitalConstants.ENTERING_OWNER_USERNAME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(HospitalConstants.ACCEPT_TO_FINISH_CREATING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("creating.finished.hospital", language));
                sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                executeMessage(sendMessage);
                SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
                sendMessage1.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
                executeMessage(sendMessage1);
                pharmacyService.markAsDone(currentProfile.getChangingElementId());
                profileService.changeStep(chatId, HospitalConstants.HOSPITAL);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("entering.hospital.location", language));
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(query.getMessage().getMessageId());
                executeEditMessage(editMessageText);
                profileService.changeStep(chatId, HospitalConstants.ENTERING_HOSPITAL_LOCATION);

            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoConstants.AUTO)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.menu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, CommonConstants.MENU);
            } else if (data.equals(AutoBoughtConstants.AUTO_BOUGHT_MENU)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
            } else if (data.equals(AutoSalonConstants.SALON)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.SALON);
            } else if (data.equals(AutoServicesConstants.AUTO_SERVICES_MENU)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.services.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoServicesMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoServicesConstants.AUTO_SERVICES_MENU);
            } else if (data.equals(AutoSparePartsShopConstants.AUTO_SPARE_PARTS_SHOP)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.spare.parts.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoSparePartsMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSparePartsShopConstants.AUTO_SPARE_PARTS_SHOP);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoBoughtConstants.AUTO_BOUGHT_MENU)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoConstants.AUTO);
            } else if (data.equals(AutoBoughtConstants.CREAT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("create.new", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.CREAT);
            } else if (data.equals(AutoBoughtConstants.ADD_MEDIA)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_ID_TO_ADD_MEDIA);
            } else if (data.equals(AutoBoughtConstants.BLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_ID_TO_BLOCK);
            } else if (data.equals(AutoBoughtConstants.UNBLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_ID_TO_UNBLOCK);
            } else if (data.equals(AutoBoughtConstants.GET_ALL)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                sendAutoList(query.getMessage(), chatId, language, null);
                profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
            } else if (data.equals(AutoBoughtConstants.GET_BY_ID)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_ID_TO_GET_AUTO);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoBoughtConstants.CREAT)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                Long autoId = autoService.createAuto(chatId);
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("automobile.id", language) + " " + autoId));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.sale.type", language));
                sendMessage.setReplyMarkup(markUps.sellType(language));
                executeMessage(sendMessage);
                profileService.changeChangingElementId(chatId, autoId);
                profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_SELL_TYPE);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoBoughtConstants.CHOOSE_SELL_TYPE)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
            } else if ("SALE,RENT,ALL".contains(data)) {
                autoService.setSaleType(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.auto.type", language));
                sendMessage.setReplyMarkup(markUpsAdmin.carType(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_AUTO_TYPE);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoBoughtConstants.CHOOSE_AUTO_TYPE)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.sale.type", language));
                sendMessage.setReplyMarkup(markUps.sellType(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_SELL_TYPE);
            } else if ("CAR,TRUCK".contains(data)) {
                autoService.setCarType(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_START_TIME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoBoughtConstants.CHOOSE_START_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.auto.type", language));
                sendMessage.setReplyMarkup(markUpsAdmin.carType(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_SELL_TYPE);
            } else if (data.endsWith(":00")) {
                autoService.setStartTime(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.end.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_END_TIME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoBoughtConstants.CHOOSE_END_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.CHOOSE_SELL_TYPE);
            } else if (data.endsWith(":00")) {
                autoService.setEndTime(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.city", language));
                sendMessage.setReplyMarkup(markUps.getBackButton(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_CITY);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoBoughtConstants.ACCEPT_TO_FINISH_CREATING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                autoService.changeStatus(ActiveStatus.ACTIVE, currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoBoughtConstants.AUTO_BOUGHT_MENU);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.location", language)));
                profileService.changeStep(chatId, AutoBoughtConstants.ENTER_LOCATION);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoSalonConstants.SALON)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.CREAT);
            } else if (data.equals(AutoSalonConstants.CREAT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("create.new", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.CREAT);
            } else if (data.equals(AutoSalonConstants.ADD_MEDIA)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_ID_TO_ADD_MEDIA);
            } else if (data.equals(AutoSalonConstants.BLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_ID_TO_BLOCK);
            } else if (data.equals(AutoSalonConstants.UNBLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_ID_TO_UNBLOCK);
            } else if (data.equals(AutoSalonConstants.GET_ALL)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                sendAutoSalonList(query.getMessage(), chatId, language, null);
                profileService.changeStep(chatId, AutoSalonConstants.SALON);
            } else if (data.equals(AutoSalonConstants.GET_BY_ID)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_ID_TO_GET_SALON);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoSalonConstants.CREAT)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                Long salonId = autoSalonService.createSalon(chatId);
                profileService
                        .changeChangingElementId(chatId, salonId);
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("create.new", language) + " id :: " + salonId));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_START_TIME);

            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.SALON);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoSalonConstants.ENTER_START_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.SALON);
            } else if (data.endsWith(":00")) {
                autoSalonService.setStartTime(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.end.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_END_TIME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoSalonConstants.ENTER_END_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_START_TIME);
            } else if (data.endsWith(":00")) {
                autoSalonService.setEndTime(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.username", language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_USERNAME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoSalonConstants.ACCEPT_TO_FINISH_CREATING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                autoSalonService.changeStatus(ActiveStatus.ACTIVE, currentProfile.getChangingElementId());
                SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("creating.finished", language));
                sendMessage1.setReplyMarkup(new ReplyKeyboardRemove(true));
                executeMessage(sendMessage1);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSalonConstants.SALON);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.location", language)));
                profileService.changeStep(chatId, AutoSalonConstants.ENTER_LOCATION);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoServicesConstants.AUTO_SERVICES_MENU)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoConstants.AUTO);
            } else if (data.equals(AutoServicesConstants.CREAT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("create.new", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoServicesConstants.CREAT);
            } else if (data.equals(AutoServicesConstants.ADD_MEDIA)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoServicesConstants.ADD_MEDIA);
            } else if (data.equals(AutoServicesConstants.BLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoServicesConstants.BLOCK);
            } else if (data.equals(AutoServicesConstants.UNBLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoServicesConstants.UNBLOCK);
            } else if (data.equals(AutoServicesConstants.GET_ALL)) {
                sendAutoServicesList(query.getMessage(), chatId, language, null);
                profileService.changeStep(chatId, AutoServicesConstants.AUTO_SERVICES_MENU);
            } else if (data.equals(AutoServicesConstants.GET_BY_ID)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoServicesConstants.GET_BY_ID);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoServicesConstants.CREAT)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                Long salonId = autoServicesService.createService(chatId);
                profileService
                        .changeChangingElementId(chatId, salonId);
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("create.new", language) + " id :: " + salonId));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.services", language) + ")");
                sendMessage.setReplyMarkup(markUpsAdmin.autoServiceType(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoServicesConstants.CHOOSE_SERVICE_TYPE);

            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.services.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoServicesConstants.AUTO_SERVICES_MENU);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoServicesConstants.CHOOSE_SERVICE_TYPE)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.services.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoServicesConstants.AUTO_SERVICES_MENU);
            } else if (data.equals(CommonConstants.NEXT)) {
                if (autoServicesService.getAllById(currentProfile.getChangingElementId()).size()==0) {
                    return;
                }
                executeDeleteMessage(new DeleteMessage(chatId,query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId,AutoServicesConstants.ENTER_START_TIME);
            } else if (data.equals(AutoServicesConstants.METHANE) ||
                    data.equals(AutoServicesConstants.PROPANE) ||
                    data.equals(AutoServicesConstants.GASOLINE) ||
                    data.equals(AutoServicesConstants.ELECTRIC_VEHICLE_CHARGING_STATION) ||
                    data.equals(AutoServicesConstants.CAR_WASH) ||
                    data.equals(AutoServicesConstants.AUTOTUNING_AND_DETAILING) ||
                    data.equals(AutoServicesConstants.TOW_TRUCK) ||
                    data.equals(AutoServicesConstants.OIL_CHANGE) ||
                    data.equals(AutoServicesConstants.AUTO_BODY_BUILDER) ||
                    data.equals(AutoServicesConstants.VULCANIZATION) ||
                    data.equals(AutoServicesConstants.ELECTRICIAN) ||
                    data.equals(AutoServicesConstants.OTHER)) {
                autoServicesService.checkServiceType(data, currentProfile.getChangingElementId());
                StringBuilder builder = new StringBuilder();
                for (AutomobileServiceTypeDTO dto : autoServicesService.getAllById(currentProfile.getChangingElementId())) {
                    if (dto.getServiceName().equals(AutoServicesConstants.METHANE)) {
                        builder.append(resourceBundleService.getMessage("methane", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.PROPANE)) {
                        builder.append(resourceBundleService.getMessage("propane", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.GASOLINE)) {
                        builder.append(resourceBundleService.getMessage("gasoline", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.ELECTRIC_VEHICLE_CHARGING_STATION)) {
                        builder.append(resourceBundleService.getMessage("electric.vehicle.charging.station", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.CAR_WASH)) {
                        builder.append(resourceBundleService.getMessage("car.wash", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.AUTOTUNING_AND_DETAILING)) {
                        builder.append(resourceBundleService.getMessage("autotuning.and.detailing", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.TOW_TRUCK)) {
                        builder.append(resourceBundleService.getMessage("tow.truck", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.OIL_CHANGE)) {
                        builder.append(resourceBundleService.getMessage("oil.change", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.AUTO_BODY_BUILDER)) {
                        builder.append(resourceBundleService.getMessage("auto.body.builder", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.VULCANIZATION)) {
                        builder.append(resourceBundleService.getMessage("vulcanization", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.ELECTRICIAN)) {
                        builder.append(resourceBundleService.getMessage("electrician", language));
                    } else if (dto.getServiceName().equals(AutoServicesConstants.OTHER)) {
                        builder.append(resourceBundleService.getMessage("other", language));
                    }
                    builder.append(", ");
                }
                String text = resourceBundleService.getMessage("choose.services", language);
                if (builder.length() == 0) {
                    text = text + ")";
                } else {
                    text=text+builder.substring(0,builder.length()-2)+")";
                }
                executeDeleteMessage(new DeleteMessage(chatId,query.getMessage().getMessageId()));
                SendMessage message=new SendMessage(chatId,text);
                message.setReplyMarkup(markUpsAdmin.autoServiceType(language));
                executeMessage(message);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId,query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(AutoServicesConstants.ENTER_START_TIME)) {

        } else if (currentStep.equals(AutoSparePartsShopConstants.AUTO_SPARE_PARTS_SHOP)) {
            //4.AutoService
            //        1.Creat
            //           acsept -> AutomobileServiceType(s) -> carType -> startTime -> endTime
            //           -> city -> BrandName -> phone
            //           -> username -> info -> (district) -> location
            //        2.Add Photo
            //           id -> photo
            //        3.Add Video
            //           id -> video
            //        4.Make Block
            //           id
            //        5.Make UnBlock
            //           id
            //        6.Take All
            //        7.Take By id
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.autoMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoConstants.AUTO);
            } else if (data.equals(AutoSparePartsShopConstants.CREAT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("create.new", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, AutoSparePartsShopConstants.CREAT);
            } else if (data.equals(AutoSparePartsShopConstants.ADD_MEDIA)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoSparePartsShopConstants.ADD_MEDIA);
            } else if (data.equals(AutoSparePartsShopConstants.BLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoSparePartsShopConstants.BLOCK);
            } else if (data.equals(AutoSparePartsShopConstants.UNBLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoSparePartsShopConstants.UNBLOCK);
            } else if (data.equals(AutoSparePartsShopConstants.GET_ALL)) {
                sendAutoSparePartsShopList(query.getMessage(), chatId, language, null);
                profileService.changeStep(chatId, AutoSparePartsShopConstants.AUTO_SPARE_PARTS_SHOP);
            } else if (data.equals(AutoSparePartsShopConstants.GET_BY_ID)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, AutoSparePartsShopConstants.GET_BY_ID);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(ShopConstants.SHOP)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.menu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, CommonConstants.MENU);
            } else if (data.equals(ShopConstants.CREAT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("accept.to.creating", language));
                sendMessage.setReplyMarkup(markUps.getAccept(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.ACCEPT_TO_CREAT);
            } else if (data.equals(ShopConstants.ADD_MEDIA)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, ShopConstants.ADD_MEDIA);
            } else if (data.equals(ShopConstants.BLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, ShopConstants.BLOCK);
            } else if (data.equals(ShopConstants.UNBLOCK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, ShopConstants.UNBLOCK);
            } else if (data.equals(ShopConstants.GET_ALL)) {
                sendShopList(query.getMessage(), chatId, language, null);
                profileService.changeStep(chatId, ShopConstants.SHOP);
            } else if (data.equals(ShopConstants.GET_BY_ID)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.id", language)));
                profileService.changeStep(chatId, ShopConstants.GET_BY_ID);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(ShopConstants.ACCEPT_TO_CREAT)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                Long salonId = shopService.createShop(chatId);
                profileService
                        .changeChangingElementId(chatId, salonId);
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("create.new", language) + " id :: " + salonId));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.shop.type", language));
                sendMessage.setReplyMarkup(markUpsAdmin.shopType(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.CHOOSE_SHOP_TYPE);


            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.SHOP);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(ShopConstants.CHOOSE_SHOP_TYPE)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage message = new SendMessage(chatId, resourceBundleService.getMessage("creating.finished", language));
                message.setReplyMarkup(new ReplyKeyboardRemove(true));
                executeMessage(message);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.SHOP);
            } else if ("FOOD_SHOP,VEGETABLES_SHOP,CLOTHES_SHOP".contains(data)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                shopService.setShopType(data, currentProfile.getChangingElementId());
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.ENTER_START_TIME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(ShopConstants.ENTER_START_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.shop.type", language));
                sendMessage.setReplyMarkup(markUpsAdmin.shopType(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.CHOOSE_SHOP_TYPE);
            } else if (data.endsWith(":00")) {
                shopService.setStartTime(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.end.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.ENTER_END_TIME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(ShopConstants.ENTER_END_TIME)) {
            if (data.equals(CommonConstants.BACK)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("choose.start.time", language));
                sendMessage.setReplyMarkup(markUps.time());
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.ENTER_START_TIME);
            } else if (data.endsWith(":00")) {
                shopService.setEndTime(data, currentProfile.getChangingElementId());
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("enter.username", language));
                sendMessage.setReplyMarkup(markUps.getBackButton(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.ENTER_USERNAME);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        } else if (currentStep.equals(ShopConstants.ACCEPT_TO_FINISH_CREATING)) {
            if (data.equals(SuperAdminConstants.ACCEPT)) {
                autoSalonService.changeStatus(ActiveStatus.ACTIVE, currentProfile.getChangingElementId());
                SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("creating.finished", language));
                sendMessage1.setReplyMarkup(new ReplyKeyboardRemove(true));
                executeMessage(sendMessage1);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
                executeMessage(sendMessage);
                profileService.changeStep(chatId, ShopConstants.SHOP);
            } else if (data.equals(SuperAdminConstants.NO_ACCEPT)) {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
                executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("enter.location", language)));
                profileService.changeStep(chatId, ShopConstants.ENTER_LOCATION);
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, query.getMessage().getMessageId()));
            }
        }

    }


    //================================ Sending XLSX (Excel files)========

    private void sendAutoSparePartsShopList(Message message, String chatId, Language language, List<AutoSparePartsShopDTO> autoSparePartsShopDTOS) {
        // todo
    }

    private void sendAutoServicesList(Message message, String chatId, Language language, List<AutomobileServiceDTO> automobileServiceDTOS) {
        // todo
    }

    private void sendShopList(Message message, String chatId, Language language, List<ShopDTO> shopDTOList) {
        if (shopDTOList == null) {
            shopDTOList = shopService.getAll();
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
        cellPharmacyType.setCellValue("createdDateTime");

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
        cellPharmacyName.setCellValue("ownerChatId");

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

        XSSFCell cellCity = row1.createCell(14);
        cellCity.setCellStyle(cellStyle);
        cellCity.setCellValue("city");

        XSSFCell cellDistrict = row1.createCell(15);
        cellDistrict.setCellStyle(cellStyle);
        cellDistrict.setCellValue("district");

        XSSFCell cellBrand = row1.createCell(16);
        cellBrand.setCellStyle(cellStyle);
        cellBrand.setCellValue("brand");

        int i = 0;
        for (
                ShopDTO shopDTO : shopDTOList) {
            if (shopDTO.getLatitude() != null) {
                XSSFRow row = autoSheet.createRow(++i);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(shopDTO.getId());
                XSSFCell cell1 = row.createCell(1);
                cell1.setCellValue(shopDTO.getCreatedDateTime().toString());
                XSSFCell cell2 = row.createCell(2);
                cell2.setCellValue(shopDTO.getStartTime().toString());
                XSSFCell cell3 = row.createCell(3);
                cell3.setCellValue(shopDTO.getEndTime().toString());
                XSSFCell cell4 = row.createCell(4);
                cell4.setCellValue(shopDTO.getUsername());
                XSSFCell cell5 = row.createCell(5);
                cell5.setCellValue(shopDTO.getPhone());
                XSSFCell cell6 = row.createCell(6);
                cell6.setCellValue(shopDTO.getOwnerChatId());
                XSSFCell cell7 = row.createCell(7);
                cell7.setCellValue(shopDTO.getInfoUz());
                XSSFCell cell8 = row.createCell(8);
                cell8.setCellValue(shopDTO.getInfoTr());
                XSSFCell cell9 = row.createCell(9);
                cell9.setCellValue(shopDTO.getInfoRu());
                XSSFCell cell10 = row.createCell(10);
                cell10.setCellValue(shopDTO.getInfoEn());
                XSSFCell cell11 = row.createCell(11);
                cell11.setCellValue(shopDTO.getLatitude());
                XSSFCell cell12 = row.createCell(12);
                cell12.setCellValue(shopDTO.getLongitude());
                XSSFCell cell13 = row.createCell(13);
                cell13.setCellValue(shopDTO.getActiveStatus().name());
                XSSFCell cell14 = row.createCell(14);
                cell14.setCellValue(shopDTO.getCity());
                XSSFCell cell15 = row.createCell(15);
                cell15.setCellValue(shopDTO.getDistrict());
                XSSFCell cell16 = row.createCell(16);
                cell16.setCellValue(shopDTO.getBrand());
            }
        }
        try {
            workbook.write(new FileOutputStream("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\shop.xlsx"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SendDocument sendDocument = new SendDocument(chatId, new InputFile(new File("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\shop.xlsx")));
        executeDocument(sendDocument);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("shop.menu", language));
        sendMessage.setReplyMarkup(markUpsAdmin.shopMenu(language));
        executeMessage(sendMessage);

    }

    private void sendAutoSalonList(Message message, String chatId, Language language, List<AutoSalonDTO> autoSalonDTOList) {
        if (autoSalonDTOList == null) {
            autoSalonDTOList = autoSalonService.getAll();
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
        cellPharmacyType.setCellValue("createdDateTime");

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
        cellPharmacyName.setCellValue("ownerChatId");

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

        XSSFCell cellSalonName = row1.createCell(14);
        cellSalonName.setCellStyle(cellStyle);
        cellSalonName.setCellValue("salonName");

        int i = 0;
        for (
                AutoSalonDTO autoSalon : autoSalonDTOList) {
            if (autoSalon.getLatitude() != null) {
                XSSFRow row = autoSheet.createRow(++i);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(autoSalon.getId());
                XSSFCell cell1 = row.createCell(1);
                cell1.setCellValue(autoSalon.getCreatedDateTime().toString());
                XSSFCell cell2 = row.createCell(2);
                cell2.setCellValue(autoSalon.getStartTime().toString());
                XSSFCell cell3 = row.createCell(3);
                cell3.setCellValue(autoSalon.getEndTime().toString());
                XSSFCell cell4 = row.createCell(4);
                cell4.setCellValue(autoSalon.getUsername());
                XSSFCell cell5 = row.createCell(5);
                cell5.setCellValue(autoSalon.getPhone());
                XSSFCell cell6 = row.createCell(6);
                cell6.setCellValue(autoSalon.getOwnerChatId());
                XSSFCell cell7 = row.createCell(7);
                cell7.setCellValue(autoSalon.getInfoUz());
                XSSFCell cell8 = row.createCell(8);
                cell8.setCellValue(autoSalon.getInfoTr());
                XSSFCell cell9 = row.createCell(9);
                cell9.setCellValue(autoSalon.getInfoRu());
                XSSFCell cell10 = row.createCell(10);
                cell10.setCellValue(autoSalon.getInfoEn());
                XSSFCell cell11 = row.createCell(11);
                cell11.setCellValue(autoSalon.getLatitude());
                XSSFCell cell12 = row.createCell(12);
                cell12.setCellValue(autoSalon.getLongitude());
                XSSFCell cell13 = row.createCell(13);
                cell13.setCellValue(autoSalon.getActiveStatus().name());
                XSSFCell cell14 = row.createCell(14);
                cell14.setCellValue(autoSalon.getSalonName());
            }
        }
        try {
            workbook.write(new FileOutputStream("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src/main/resources/autoSalon.xlsx"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SendDocument sendDocument = new SendDocument(chatId, new InputFile(new File("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\autoSalon.xlsx")));
        executeDocument(sendDocument);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.salon.menu", language));
        sendMessage.setReplyMarkup(markUpsAdmin.autoSalonMenu(language));
        executeMessage(sendMessage);

    }

    private void sendAutoList(Message message, String chatId, Language language, List<AutomobileDTO> automobileList) {
        if (automobileList == null) {
            automobileList = autoService.getAll();
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet autoSheet = workbook.createSheet("hospital");

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
        cellPharmacyType.setCellValue("carType");

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
        cellPharmacyName.setCellValue("salaryType");

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

        XSSFCell cellBrandName = row1.createCell(16);
        cellBrandName.setCellStyle(cellStyle);
        cellBrandName.setCellValue("brandName");

        XSSFCell cellModel = row1.createCell(17);
        cellModel.setCellStyle(cellStyle);
        cellModel.setCellValue("model");

        XSSFCell cellPrice = row1.createCell(18);
        cellPrice.setCellStyle(cellStyle);
        cellPrice.setCellValue("price");

        XSSFCell cellCity = row1.createCell(19);
        cellCity.setCellStyle(cellStyle);
        cellCity.setCellValue("city");

        XSSFCell cellDistrict = row1.createCell(20);
        cellDistrict.setCellStyle(cellStyle);
        cellDistrict.setCellValue("district");

        int i = 0;
        for (
                AutomobileDTO automobile : automobileList) {
            if (automobile.getLatitude() != null) {
                XSSFRow row = autoSheet.createRow(++i);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(automobile.getId());
                XSSFCell cell1 = row.createCell(1);
                cell1.setCellValue(automobile.getCarType().name());
                XSSFCell cell2 = row.createCell(2);
                cell2.setCellValue(automobile.getStartTime().toString());
                XSSFCell cell3 = row.createCell(3);
                cell3.setCellValue(automobile.getEndTime().toString());
                XSSFCell cell4 = row.createCell(4);
                cell4.setCellValue(automobile.getUsername());
                XSSFCell cell5 = row.createCell(5);
                cell5.setCellValue(automobile.getPhone());
                XSSFCell cell6 = row.createCell(6);
                cell6.setCellValue(automobile.getSalaryType().name());
                XSSFCell cell7 = row.createCell(7);
                cell7.setCellValue(automobile.getInfoUz());
                XSSFCell cell8 = row.createCell(8);
                cell8.setCellValue(automobile.getInfoTr());
                XSSFCell cell9 = row.createCell(9);
                cell9.setCellValue(automobile.getInfoRu());
                XSSFCell cell10 = row.createCell(10);
                cell10.setCellValue(automobile.getInfoEn());
                XSSFCell cell11 = row.createCell(11);
                cell11.setCellValue(automobile.getLatitude());
                XSSFCell cell12 = row.createCell(12);
                cell12.setCellValue(automobile.getLongitude());
                XSSFCell cell13 = row.createCell(13);
                cell13.setCellValue(automobile.getActiveStatus().name());
                XSSFCell cell14 = row.createCell(14);
                cell14.setCellValue(automobile.getCreatedDateTime().toString());
                XSSFCell cell15 = row.createCell(15);
                cell15.setCellValue(automobile.getOwnerChatId());
                XSSFCell cell16 = row.createCell(16);
                cell16.setCellValue(automobile.getBrandName());
                XSSFCell cell17 = row.createCell(17);
                cell17.setCellValue(automobile.getModel());
                XSSFCell cell18 = row.createCell(18);
                cell18.setCellValue(automobile.getPrice());
                XSSFCell cell19 = row.createCell(19);
                cell19.setCellValue(automobile.getCity());
                XSSFCell cell20 = row.createCell(20);
                cell20.setCellValue(automobile.getDistrict());
            }
        }
        try {
            workbook.write(new FileOutputStream("./src/main/resources/automobile.xlsx"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument = new SendDocument(chatId, new InputFile(new File("./src/main/resources/automobile.xlsx")));
        executeDocument(sendDocument);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("auto.bought.menu", language));
        sendMessage.setReplyMarkup(markUpsAdmin.autoBuyMenu(language));
        executeMessage(sendMessage);
    }

    private void sendHospitalList(Message message, String chatId, Language language, List<HospitalDTO> hospitalList) {
        if (hospitalList == null) {
            hospitalList = hospitalService.getAll();
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet autoSheet = workbook.createSheet("hospital");

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
        cellPharmacyType.setCellValue("hospitalService");

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
        cellPharmacyName.setCellValue("hospitalName");

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
                HospitalDTO hospital : hospitalList) {
            if (hospital.getLatitude() != null) {
                XSSFRow row = autoSheet.createRow(++i);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue(hospital.getId());
                StringBuilder hospitalServices = new StringBuilder();
                for (HospitalServiceDTO hospitalServiceDTO : hospital.getHospitalService()) {
                    hospitalServices.append(hospitalServiceDTO.getServiceName()).append(", ");
                }
                if (hospitalServices.length() > 0) {
                    hospitalServices = new StringBuilder(hospitalServices.substring(0, hospitalServices.length() - 2));
                }
                XSSFCell cell1 = row.createCell(1);
                cell1.setCellValue(hospitalServices.toString());
                XSSFCell cell2 = row.createCell(2);
                cell2.setCellValue(hospital.getStartTime().toString());
                XSSFCell cell3 = row.createCell(3);
                cell3.setCellValue(hospital.getEndTime().toString());
                XSSFCell cell4 = row.createCell(4);
                cell4.setCellValue(hospital.getUsername());
                XSSFCell cell5 = row.createCell(5);
                cell5.setCellValue(hospital.getPhone());
                XSSFCell cell6 = row.createCell(6);
                cell6.setCellValue(hospital.getHospitalName());
                XSSFCell cell7 = row.createCell(7);
                cell7.setCellValue(hospital.getInfoUz());
                XSSFCell cell8 = row.createCell(8);
                cell8.setCellValue(hospital.getInfoTr());
                XSSFCell cell9 = row.createCell(9);
                cell9.setCellValue(hospital.getInfoRu());
                XSSFCell cell10 = row.createCell(10);
                cell10.setCellValue(hospital.getInfoEn());
                XSSFCell cell11 = row.createCell(11);
                cell11.setCellValue(hospital.getLatitude());
                XSSFCell cell12 = row.createCell(12);
                cell12.setCellValue(hospital.getLongitude());
                XSSFCell cell13 = row.createCell(13);
                cell13.setCellValue(hospital.getActiveStatus().name());
                XSSFCell cell14 = row.createCell(14);
                cell14.setCellValue(hospital.getCreatedDateTime().toString());
                XSSFCell cell15 = row.createCell(15);
                cell15.setCellValue(hospital.getOwnerChatId());
            }
        }
        try {
            workbook.write(new FileOutputStream("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\hospital.xlsx"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument = new SendDocument(chatId, new InputFile(new File("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\hospital.xlsx")));
        executeDocument(sendDocument);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("hospital.menu", language));
        sendMessage.setReplyMarkup(markUpsAdmin.hospitalMenu(language));
        executeMessage(sendMessage);

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
                executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
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
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
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
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
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
            } else {
                executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
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

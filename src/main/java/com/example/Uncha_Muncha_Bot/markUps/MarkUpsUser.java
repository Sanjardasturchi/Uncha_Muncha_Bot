package com.example.Uncha_Muncha_Bot.markUps;

import com.example.Uncha_Muncha_Bot.constants.*;
import com.example.Uncha_Muncha_Bot.enums.HospitalService;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.service.ResourceBundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.LinkedList;
import java.util.List;

@Component
public class MarkUpsUser {
    @Autowired
    private ResourceBundleService resourceBundleService;

    /**
     * InlineKeyboardMarkup for User Menu
     */
    public ReplyKeyboard menu(Language language) {
        List<InlineKeyboardButton> buttonsRow = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("pharmacy.menu", language));
        button.setCallbackData(PharmacyConstants.PHARMACY);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("hospital.menu", language));
        button.setCallbackData(HospitalConstants.HOSPITAL);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("auto.menu", language));
        button.setCallbackData(AutoConstants.AUTO);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("house.menu", language));
        button.setCallbackData(HouseConstants.HOUSE);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("shop.menu", language));
        button.setCallbackData(ShopConstants.SHOP);

        buttonsRow.add(button);
        rowList.add(buttonsRow);

        return new InlineKeyboardMarkup(rowList);
    }

    /**
     * Pharmacy menu (People,Animals,Back) Inline buttons
     */
    public InlineKeyboardMarkup hospitalType(Language language) {
        List<InlineKeyboardButton> buttonsRow = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("dentist", language));
        button.setCallbackData(HospitalService.DENTIST.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("lor", language));
        button.setCallbackData(HospitalService.LOR.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("pediatrician", language));
        button.setCallbackData(HospitalService.PEDIATRICIAN.name());

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("cardiologist", language));
        button.setCallbackData(HospitalService.CARDIOLOGIST.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("dermatologist", language));
        button.setCallbackData(HospitalService.DERMATOLOGIST.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("neurologist", language));
        button.setCallbackData(HospitalService.NEUROLOGIST.name());

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("ophthalmologist", language));
        button.setCallbackData(HospitalService.OPHTHALMOLOGIST.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("orthopedist", language));
        button.setCallbackData(HospitalService.ORTHOPEDIST.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("urologist", language));
        button.setCallbackData(HospitalService.UROLOGIST.name());

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("dietician", language));
        button.setCallbackData(HospitalService.DIETICIAN.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("psychologist", language));
        button.setCallbackData(HospitalService.PSYCHOLOGIST.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("vet", language));
        button.setCallbackData(HospitalService.VET.name());


        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("back", language));
        button.setCallbackData(CommonConstants.BACK);

        buttonsRow.add(button);
        rowList.add(buttonsRow);

        return new InlineKeyboardMarkup(rowList);
    }
}

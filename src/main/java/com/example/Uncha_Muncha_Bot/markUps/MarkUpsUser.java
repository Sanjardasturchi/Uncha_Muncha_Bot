package com.example.Uncha_Muncha_Bot.markUps;

import com.example.Uncha_Muncha_Bot.constants.*;
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

    /**InlineKeyboardMarkup for User Menu*/
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

    /**Pharmacy menu (People,Animals,Back) Inline buttons*/
    public InlineKeyboardMarkup pharmacyMenu(Language language) {
        List<InlineKeyboardButton> buttonsRow = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("pharmacy.for.people", language));
        button.setCallbackData(PharmacyConstants.PHARMACY_FOR_PEOPLE);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("pharmacy.for.animals", language));
        button.setCallbackData(PharmacyConstants.PHARMACY_FOR_ANIMALS);

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

package com.example.Uncha_Muncha_Bot.markUps;

import com.example.Uncha_Muncha_Bot.constants.*;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.service.ResourceBundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.LinkedList;
import java.util.List;
@Component
public class MarkUpsSuperAdmin {
    @Autowired
    private ResourceBundleService resourceBundleService;

    /**InlineKeyboardMarkup for Super Admin Menu*/
    public ReplyKeyboard menu(Language language) {
        List<InlineKeyboardButton> buttonsRow = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText(resourceBundleService.getMessage("make.admin", language));
        button.setCallbackData(SuperAdminConstants.MAKE_ADMIN);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("make.user", language));
        button.setCallbackData(SuperAdminConstants.MAKE_USER);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("make.block", language));
        button.setCallbackData(SuperAdminConstants.MAKE_BLOCK);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("make.active", language));
        button.setCallbackData(SuperAdminConstants.MAKE_ACTIVE);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("get.all.profile", language));
        button.setCallbackData(SuperAdminConstants.GET_ALL);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("get.all.admin", language));
        button.setCallbackData(SuperAdminConstants.GET_ALL_ADMIN);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("get.all.user", language));
        button.setCallbackData(SuperAdminConstants.GET_ALL_USER);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("get.by.chat.id", language));
        button.setCallbackData(SuperAdminConstants.GET_BY_CHAT_ID);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("advertising.placement", language));
        button.setCallbackData(SuperAdminConstants.CREATE_ADVERTISING);

        buttonsRow.add(button);
        rowList.add(buttonsRow);
        button = new InlineKeyboardButton();
        buttonsRow = new LinkedList<>();

        button.setText(resourceBundleService.getMessage("get.by.f.id", language));
        button.setCallbackData(SuperAdminConstants.GET_BY_F_ID);

        buttonsRow.add(button);
        rowList.add(buttonsRow);

        return new InlineKeyboardMarkup(rowList);
    }

    public InlineKeyboardMarkup getAccept(Language language) {
        List<InlineKeyboardButton> buttonsRow = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText("✅");
        button.setCallbackData(SuperAdminConstants.ACCEPT);

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText("❌");
        button.setCallbackData(SuperAdminConstants.NO_ACCEPT);

        buttonsRow.add(button);
        rowList.add(buttonsRow);

        return new InlineKeyboardMarkup(rowList);
    }


}

package com.example.Uncha_Muncha_Bot.markUps;

import com.example.Uncha_Muncha_Bot.constants.CommonConstants;
import com.example.Uncha_Muncha_Bot.constants.SuperAdminConstants;
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
public class MarkUps {
    @Autowired
    private ResourceBundleService resourceBundleService;

    private List<String> timeList = List.of("00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
            "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00", "23:00");

    public ReplyKeyboard adminButton(Language language) {
        return null;
    }

    public ReplyKeyboard language() {
        List<InlineKeyboardButton> buttonsRow = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText(Language.uz.name()+" \uD83C\uDDFA\uD83C\uDDFF");
        button.setCallbackData(Language.uz.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(Language.tr.name()+" \uD83C\uDDF9\uD83C\uDDF7");
        button.setCallbackData(Language.tr.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(Language.ru.name()+" \uD83C\uDDF7\uD83C\uDDFA");
        button.setCallbackData(Language.ru.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(Language.en.name()+" \uD83C\uDDEC\uD83C\uDDE7");
        button.setCallbackData(Language.en.name());

        buttonsRow.add(button);
        rowList.add(buttonsRow);

        return new InlineKeyboardMarkup(rowList);
    }

    public ReplyKeyboard contactButton(Language profileLanguage) {
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(resourceBundleService.getMessage("sharing.phone.number",profileLanguage));
        button.setRequestContact(true);
        row.add(button);
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboard getNextAndBackButtons(Language language) {
        KeyboardRow row = new KeyboardRow();

        KeyboardButton button = new KeyboardButton(resourceBundleService.getMessage("back",language));
        row.add(button);
        button = new KeyboardButton(resourceBundleService.getMessage("next",language));
        row.add(button);

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboard getBackButton(Language language) {
        KeyboardRow row = new KeyboardRow();

        KeyboardButton button = new KeyboardButton(resourceBundleService.getMessage("back",language));
        row.add(button);

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
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

    public InlineKeyboardMarkup time() {
        List<InlineKeyboardButton> buttons = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();

        int count = 0;
        // timeList(8:00,9:00,10:00)
        for (String time : timeList) {
            count++;
            button.setText(time);
            button.setCallbackData(time);

            buttons.add(button);
            button = new InlineKeyboardButton();
            if (count % 3 == 0 || count == timeList.size()) {
                rowList.add(buttons);
                buttons = new LinkedList<>();
            }
        }
        button.setText(CommonConstants.BACK);
        button.setCallbackData(CommonConstants.BACK);
        buttons.add(button);
        rowList.add(buttons);
        return new InlineKeyboardMarkup(rowList);
    }
}

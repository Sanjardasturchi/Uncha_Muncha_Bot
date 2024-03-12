package com.example.Uncha_Muncha_Bot.service;


public class GeneralService {
    public static String checkMoneyFromTheString(String text) {
        StringBuilder newText=new StringBuilder();
        String[] spaces = text.split(" ");
        for (String space : spaces) {
            newText.append(space);
        }
        text=String.valueOf(newText);
        newText.delete(0,newText.length());

        String[] dots = text.split("\\.");
        for (String dot : dots) {
            newText.append(dot);
        }
        text=String.valueOf(newText);
        newText.delete(0,newText.length());

        String[] commas = text.split(",");
        for (String comma : commas) {
            newText.append(comma);
        }
        text=String.valueOf(newText);
        newText.delete(0,newText.length());

        String[] lines = text.split("_");
        for (String line : lines) {
            newText.append(line);
        }
        text=String.valueOf(newText);

        return text;
    }
}

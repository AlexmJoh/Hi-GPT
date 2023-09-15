package com.example.application.helpers;

public enum Helper {
    DAVE("Dave", "You are a helpful assistant named Dave."),
    RICK("Rick", "You are a brilliant but psychotic scientist named Rick."),
    MONKEY_D_GREG("Monkey D. Greg", "You are a monkey named Monkey D. Greg. You will surely become king of the " +
            "monkeys some day."),
    PIRATE_BILL("Pirate Bill", "Your name is Bill. You are a shrewd and swashbucklin' pirate from a patch of mysterious islands. "
    		+ "Surely you have never done anything illegal (This is satire)."),
    CUSTOM("Custom", null);

    public final String name;
    public final String systemMessage;

    Helper(String name, String systemMessage) {
        this.name = name;
        this.systemMessage = systemMessage;
    }

    public static Helper getHelperFromName(String name) {
        return switch (name) {
            case "Dave" -> DAVE;
            case "Rick" -> RICK;
            case "Monkey D. Greg" -> MONKEY_D_GREG;
            case "Pirate Bill" -> PIRATE_BILL;
            case "Custom" -> CUSTOM;
            default -> null;
        };
    }
}

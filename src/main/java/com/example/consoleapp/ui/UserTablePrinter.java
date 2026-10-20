package com.example.consoleapp.ui;

import com.example.consoleapp.entity.User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public class UserTablePrinter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ZoneId zone;


    public UserTablePrinter() {
        this(ZoneId.systemDefault());
    }

    public UserTablePrinter(ZoneId zone) {
        this.zone = Objects.requireNonNull(zone, "zone");
    }

    private record Column(String header, Function<User, String> value) {
    }

    private List<Column> columns() {
        return List.of(
                new Column("ID", u -> text(u.getId())),
                new Column("Имя", u -> text(u.getName())),
                new Column("Email", u -> text(u.getEmail())),
                new Column("Возраст", u -> text(u.getAge())),
                new Column("Создан", u -> formatInstant(u.getCreatedCode()))
        );
    }

    public String render(List<User> users) {
        if (users == null || users.isEmpty()) {
            return "Список пользователей пуст." + System.lineSeparator();
        }

        List<Column> columns = columns();
        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            int max = column.header().length();
            for (User user : users) {
                max = Math.max(max, column.value().apply(user).length());
            }
            widths[i] = max;
        }

        StringBuilder sb = new StringBuilder();
        String separator = separator(widths);
        sb.append(separator);
        appendRow(sb, widths, columns.stream().map(Column::header).toList());
        sb.append(separator);
        for (User user : users) {
            appendRow(sb, widths, columns.stream().map(c -> c.value().apply(user)).toList());
        }
        sb.append(separator);
        sb.append("Всего пользователей: ").append(users.size()).append(System.lineSeparator());
        return sb.toString();
    }


    public String renderOne(User user) {
        Objects.requireNonNull(user, "user");
        String nl = System.lineSeparator();
        return "ID:      " + text(user.getId()) + nl
                + "Имя:     " + text(user.getName()) + nl
                + "Email:   " + text(user.getEmail()) + nl
                + "Возраст: " + text(user.getAge()) + nl
                + "Создан:  " + formatInstant(user.getCreatedCode()) + nl;
    }

    private static String separator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append('+');
        }
        return sb.append(System.lineSeparator()).toString();
    }

    private static void appendRow(StringBuilder sb, int[] widths, List<String> cells) {
        sb.append('|');
        for (int i = 0; i < widths.length; i++) {
            String cell = cells.get(i);
            sb.append(' ').append(cell).append(" ".repeat(widths[i] - cell.length())).append(" |");
        }
        sb.append(System.lineSeparator());
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "" : DATE_FORMAT.format(instant.atZone(zone));
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }
}

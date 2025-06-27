package util;

public class PrintPasswordHash {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = PasswordUtils.hashPassword(password);
        System.out.println(hash);
    }
}

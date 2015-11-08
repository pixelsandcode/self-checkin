package me.tipi.self_check_in.util;



public final class Strings {
  private Strings() {
    // No instances.
  }

  public static boolean isBlank(CharSequence string) {
    return (string == null || string.toString().trim().length() == 0);
  }

  public static String valueOrDefault(String string, String defaultString) {
    return isBlank(string) ? defaultString : string;
  }

  public static String truncateAt(String string, int length) {
    return string.length() > length ? string.substring(0, length) : string;
  }

  public static String substringAt(String string, int location) {
    return string.length() > location ? string.substring(location) : string;
  }

  public static boolean isValidEmail(CharSequence target) {
    return target != null &&
        android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
  }

  public static String getPreStringSplit(String value, String splitter) {
    String[] parts = value.split(splitter);
    return parts[0];
  }

  public static String getPinCode(String data) {
    String[] parts = data.split("/");
    return parts[parts.length - 1];
  }

  public static String getPostStringSplit(String value, String splitter) {
    String[] parts = value.split(splitter);
    return parts[parts.length - 1];
  }
}

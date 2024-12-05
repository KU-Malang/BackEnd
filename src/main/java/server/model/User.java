package server.model;

public class User {

    private final int userId;
    private final String nickname;
    private final String password;
    private int rating;

    public User(int userId, String nickname, String password) {
        this.userId = userId;
        this.nickname = nickname;
        this.password = password;
        this.rating = 1000; // 기본 레이팅
    }

    public User(int userId, String nickname, String password, int rating) {
        this.userId = userId;
        this.nickname = nickname;
        this.password = password;
        this.rating = rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }

    public int getRating() {
        return rating;
    }

    public void increaseRating(int amount) {
        this.rating += amount;
    }

    public void decreaseRating(int amount) {
        this.rating -= amount;
    }
}

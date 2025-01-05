package pl.michal5520pl.wszib;

class User {
    private final String username;
    private final String email;
    private final String password;

    User(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
    }

    String getUsername(){
        return username;
    }

    String getEmail() {
        return email;
    }

    String getPassword() {
        return password;
    }
}

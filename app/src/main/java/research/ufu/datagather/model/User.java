package research.ufu.datagather.model;

public class User {
    private String username;
    private String password;

    public User(String usr, String psw){
        username = usr;
        password = psw;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
}
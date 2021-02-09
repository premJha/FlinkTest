package kafka.jsonCP;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class User {

    public static void main(String[] args) {
        User user = new User("a", "b", 1);
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuffer stringBuffer=new StringBuffer();
        try {
            //objectMapper.writeValue(System.out,user);
            String s = objectMapper.writeValueAsString(user);
            System.out.println("AA:"+s);
            User user1 = objectMapper.readValue(s, User.class);
            System.out.println(user1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //@JsonProperty
    public String firstName;

    //@JsonProperty
    public String lastName;

    // @JsonProperty
    public int age;

    public User() {}

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                '}';
    }

    public User(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName=lastName;
        this.age = age;
    }
}
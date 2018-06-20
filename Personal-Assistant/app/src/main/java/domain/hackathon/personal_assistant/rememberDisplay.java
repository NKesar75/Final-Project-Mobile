package domain.hackathon.personal_assistant;

public class rememberDisplay
{
    String type;
    String title;
    String body;

    public rememberDisplay(String type, String title, String body){
        this.type = type;
        this.title = title;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }
}
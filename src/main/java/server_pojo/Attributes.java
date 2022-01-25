package server_pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class Attributes {
    public int rice;
    public String category;
    public int level;
    public int streak;
    public ArrayList<Object> played_categories;
    public Question question;
    public Answer answer;
    public Highscores highscores;
    public ArrayList<Object> badges;
    public String question_id;
    public Userattributes userattributes;
    public ArrayList<Object> groups;
}

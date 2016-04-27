package fm.grind.android.entities.dto;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "channel", strict = false)
public class Channel {

    @ElementList(name = "item", inline = true)
    private List<Article> articleList;

    @Element
    private String title;

    @Element
    private String link;

    @Element(required = false)
    private String description;

    public List<Article> getArticleList() {
        return articleList;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "articleList=" + articleList +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
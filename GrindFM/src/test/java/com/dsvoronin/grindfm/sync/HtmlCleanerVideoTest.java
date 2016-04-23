package com.dsvoronin.grindfm.sync;

import com.dsvoronin.grindfm.BuildConfig;
import com.dsvoronin.grindfm.entities.Article;
import com.dsvoronin.grindfm.entities.Description;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class HtmlCleanerVideoTest {

    String testHtml = "<p> Стартовала регистрация команд на киберспортивный турнир серии Warface Open Cup с призовым фондом в 1,5 миллиона рублей. Наряду с профессиональными командами из Warface Masters League побороться за чемпионский титул в весеннем турнире сможет любая команда, прошедшая отборочные этапы.\n" +
            "</p>\n" +
            "<p><center><iframe width=\"800\" height=\"450\" src=\"https://www.youtube.com/embed/yi0q8WOu3f0\" frameborder=\"0\" allowfullscreen></iframe></center></p>\n" +
            "<p>Турнир пройдет в несколько этапов, по итогам которых будет определены участники LAN-финала. 18–28 апреля состоятся состязания профессиональных команд Masters League. Два лидера из Masters League попадут напрямую в «финал четырех». Еще за 2 путевки будут бороться участники стыковых матчей 14–15 мая. В них сойдутся команды, занявшие 3–6 места в Masters League, и четверка лучших коллективов открытого кубка для всех желающих Challenge Cup. Грандиозный LAN-финал Warface Open Cup: Весна-2016 пройдет в конце мая в Москве.</p>\n" +
            "<p>Популярность турниров серии Open Cup продолжает расти: тысячи игроков из России и СНГ сражаются в отборочных матчах, чтобы побороться за чемпионское звание и крупный призовой фонд. Минувшей зимой за LAN-финалом Warface Open Cup, в котором абсолютным победителем стала команда ArenaStars, следило рекордное количество онлайн-зрителей — прямой эфир решающих боев набрал более 1,8 миллионов просмотров.</p>\n" +
            "<p>Узнать подробнее о регламенте Warface Open Cup: Весна-2016 и зарегистрироваться для участия в турнире можно на официальной странице (<a href=\"https://wf.mail.ru/promo/opencupspring2016/\" title=\"https://wf.mail.ru/promo/opencupspring2016/\">https://wf.mail.ru/promo/opencupspring2016/</a>)</p>\n" +
            "<p>Warface — популярный онлайн-шутер от студии Crytek; издателем и локализатором русскоязычной версии проекта выступает Mail.Ru Group. Игроки Warface выбирают один из 4 доступных классов и выполняют PvP- и PvE-миссии в самых разных уголках планеты. За высокую детализацию и визуальные эффекты в игре отвечает технология CryENGINE. Особенную популярность Warface снискал на территории России и стран СНГ — в игре зарегистрировано более 30 млн пользователей. В январе 2013 года Warface установил рекорд Гиннесса в категории «Наибольшее количество игроков, одновременно находящихся на одном сервере онлайн-шутера».</p>\n" +
            "<p>Warface Open Cup — крупнейший официальный кибеспортивный турнир по онлайн-шутеру Warface. Каждый год проходят 4 сезона соревнований, состоящих из турнира профессиональной лиги Warface Masters League, открытого этапа Challenge Cup, стыковых матчей и LAN-финала, который проходит в Москве. Зарегистрироваться на открытый этап Challenge Cup могут все желающие: в каждом сезоне заявки на участие подают свыше 1 200 команд. LAN-финалы проходят в московском офисе Mail.Ru Group, транслируются на популярных стриминговых площадках и собирают свыше 1 800 000 просмотров.</p>\n" +
            "<!--break--><!--break--><script type=\"text/javascript\" src=\"//yastatic.net/share/share.js\" charset=\"utf-8\"></script><div style=\"float: right;\" class=\"yashare-auto-init\" data-yashareL10n=\"ru\" data-yashareType=\"icon\" data-yashareQuickServices=\"vkontakte,facebook,twitter\"></div>\n" +
            "<p><meta property=\"og:image\" content=\"http://www.grind.fm/files/grind.fm/7rTXzugqk-g.jpg\" /></p>\n";

    String expected = "Стартовала регистрация команд на киберспортивный турнир серии Warface Open Cup с призовым фондом в 1,5 миллиона рублей. Наряду с профессиональными командами из Warface Masters League побороться за чемпионский титул в весеннем турнире сможет любая команда, прошедшая отборочные этапы. \n" +
            "\n" +
            "Турнир пройдет в несколько этапов, по итогам которых будет определены участники LAN-финала. 18–28 апреля состоятся состязания профессиональных команд Masters League. Два лидера из Masters League попадут напрямую в «финал четырех». Еще за 2 путевки будут бороться участники стыковых матчей 14–15 мая. В них сойдутся команды, занявшие 3–6 места в Masters League, и четверка лучших коллективов открытого кубка для всех желающих Challenge Cup. Грандиозный LAN-финал Warface Open Cup: Весна-2016 пройдет в конце мая в Москве.\n" +
            "\n" +
            "Популярность турниров серии Open Cup продолжает расти: тысячи игроков из России и СНГ сражаются в отборочных матчах, чтобы побороться за чемпионское звание и крупный призовой фонд. Минувшей зимой за LAN-финалом Warface Open Cup, в котором абсолютным победителем стала команда ArenaStars, следило рекордное количество онлайн-зрителей — прямой эфир решающих боев набрал более 1,8 миллионов просмотров.\n" +
            "\n" +
            "Узнать подробнее о регламенте Warface Open Cup: Весна-2016 и зарегистрироваться для участия в турнире можно на официальной странице (https://wf.mail.ru/promo/opencupspring2016/)\n" +
            "\n" +
            "Warface — популярный онлайн-шутер от студии Crytek; издателем и локализатором русскоязычной версии проекта выступает Mail.Ru Group. Игроки Warface выбирают один из 4 доступных классов и выполняют PvP- и PvE-миссии в самых разных уголках планеты. За высокую детализацию и визуальные эффекты в игре отвечает технология CryENGINE. Особенную популярность Warface снискал на территории России и стран СНГ — в игре зарегистрировано более 30 млн пользователей. В январе 2013 года Warface установил рекорд Гиннесса в категории «Наибольшее количество игроков, одновременно находящихся на одном сервере онлайн-шутера».\n" +
            "\n" +
            "Warface Open Cup — крупнейший официальный кибеспортивный турнир по онлайн-шутеру Warface. Каждый год проходят 4 сезона соревнований, состоящих из турнира профессиональной лиги Warface Masters League, открытого этапа Challenge Cup, стыковых матчей и LAN-финала, который проходит в Москве. Зарегистрироваться на открытый этап Challenge Cup могут все желающие: в каждом сезоне заявки на участие подают свыше 1 200 команд. LAN-финалы проходят в московском офисе Mail.Ru Group, транслируются на популярных стриминговых площадках и собирают свыше 1 800 000 просмотров.\n" +
            "\n";

    Description description;

    @Before
    public void setup() {
        Article article = new Article();
        article.description = testHtml;
        description = new Description(article);
    }

    /**
     * Not a test, more like a demonstration of how rss description cleaned
     */
    @Test
    public void cleanText() {
        assertEquals(expected, description.getPureText());
    }

    @Test
    public void imageExtracted() {
        assertEquals("http://i1.ytimg.com/vi/yi0q8WOu3f0/maxresdefault.jpg", description.getImageUrl());
    }
}
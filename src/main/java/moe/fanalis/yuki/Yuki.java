package moe.fanalis.yuki;

import org.javatuples.Pair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.io.File;

public class Yuki {
    private static final String MANGA_URL = "https://readms.net/manga/";
    private static final String BASE_URL = "https://readms.net";

    public static void main(String[] arg){
        String f = "Shokugeki No Souma";
        f = f.replace(" ","_").toLowerCase();

        try {
            Yuki yuuki = new Yuki();/*
            List<Pair<String,String>> episodes = yuuki.getMangaEpisodes(f);

            //List<Pair<String,String>> episodes = yuuki.getMangaList();
            for(Pair<String,String> episode: episodes){
                System.out.println("Episode name: " + episode.getValue0());
                System.out.println("url: " + episode.getValue1());
            }*/
            //String url = yuuki.getMangaPage("https://readms.net/r/shokugeki_no_souma/246/4822/1");
            //System.out.println(url);
            yuuki.downloadLastEpisode("Shokugeki no Souma","output");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        //Boolean success = (new File("output/"+f)).mkdirs();
        //File file = new File("output/"+f);
        //System.out.println(file.exists() && file.isDirectory());
    }

    /**
     * Make a slug of the passed string
     * @param name
     * @return
     */
    public static String sanitizeName(String name){
        name = name.replace(" ","_").toLowerCase();
        return name;
    }

    /**
     * Return a List of pairc containing Manga name and url
     * @return Manga list of mangastream
     * @throws IOException
     */
    public List<Pair<String,String>> getMangaList() throws IOException{
        return getInfo(MANGA_URL,"a[href^=/manga/]");
    }

    /**
     * Get the episodes of that manga. Returns an empty list if the name is incorrect.
     * It'll sanitize the name, so "Shokugeki no Souma" will be something like "shokugeki_no_souma"
     * Each element of the List is a Pair<String,String> where value0 = Name and value1 = Url
     * @param name Name of the manga
     * @return Episodes of that manga in mangastream
     * @throws IOException
     */
    public List<Pair<String,String>> getMangaEpisodes(String name) throws IOException{
        name = sanitizeName(name);
        return getInfo(MANGA_URL+name,String.format("a[href^=/r/%s]",name));
    }

    /**
     * Makes a request in the desired url and returns all url (and text) of the elements that match
     * the desired pattern.
     * @param url Url to make request
     * @param startPattern Pattern to match in the <a> tag
     * @return List of content that match the pattern in that page
     * @throws IOException
     */
    private List<Pair<String,String>> getInfo(String url,String tag) throws IOException{
        Connection.Response response = Jsoup
                .connect(url)
                .followRedirects(false)
                .method(Connection.Method.GET)
                .execute();
        List<Pair<String,String>> content = new ArrayList<>();
        //If it is a valid page
        if(response.header("Location") == null){
            Document doc = response.parse();
            Elements links = doc.select(tag);
            for(Element link: links){
                Pair<String,String> episode = new Pair<>(link.text(),link.attr("href"));
                content.add(episode);
            }
        }
        return content;

    }

    private String simpleGetInfo(String url, String tag,String desiredPart) throws IOException{
        Connection.Response response = Jsoup
                .connect(url)
                .followRedirects(false)
                .method(Connection.Method.GET)
                .execute();
        String content = null;
        //If it is a valid page
        if(response.header("Location") == null){
            Document doc = response.parse();
            Element link = doc.select(tag).first();
            content =  link.attr(desiredPart);
        }
        return content;
    }

    /**
     * Get the manga page in the desired url
     * @param url
     * @return
     * @throws IOException
     */
    private String getMangaPage(String url) throws IOException{
        String info = simpleGetInfo(url,"img#manga-page","src");
        info = info == null? null: "https:"+info;
        return info;
    }

    /**
     * Saves the image of the desired url, full path to file is required
     * @param url
     * @param path
     */
    private void saveImage(String url,String path) throws IOException{
        Connection.Response resp = Jsoup
                .connect(url)
                .ignoreContentType(true)
                .execute();
        FileOutputStream out = (new FileOutputStream(new File(path)));
        out.write(resp.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
        out.close();
    }

    /**
     * Downloads the last episode of desired manga in desired folder
     * @param manga
     * @param path
     */
    public void downloadLastEpisode(String manga,String path) throws Exception{
        String page;
        manga = sanitizeName(manga);
        int currentPageName = 1;
        String url = BASE_URL + simpleGetInfo(MANGA_URL,String.format("a.chapter-link[href*=%s]",manga),"href");
        String[] splitted = url.split("/");
        int episode = Integer.valueOf(splitted[5]); //Get the current episode
        int currentPage = Integer.parseInt(splitted[splitted.length -1]); //Get the current page
        File dir = new File(String.format("%s/%s/%d",path,manga,episode));
        //If the directory doesn't exist, create it
        if(!dir.exists() || !dir.isDirectory()){
            Boolean success = dir.mkdirs();
            if(!success){
                throw new Exception(String.format("Failed to create te directory: %s/%s/%d",path,manga,episode));
            }
        }
        do {
            //String joined = String.join("/",splitted);
            page = getMangaPage(url);
            if(page != null){
                System.out.println(String.format("Downloading page %d",currentPageName));
                String[] splittedPage = page.split("\\.");
                String extension = splittedPage[splittedPage.length-1]; //Get the extension of the file
                splitted[splitted.length-1] = String.valueOf(Integer.valueOf(splitted[splitted.length-1])+1); //Increment the page
                //Save the image
                saveImage(page,String.format("%s/%s/%s/0%d.%s",path,manga,episode,currentPageName,extension));
                //Build the new url
                url = String.join("/",splitted);
                System.out.println("Url now is " + url);
                currentPageName++;
            }
        }while(page != null);
    }
}

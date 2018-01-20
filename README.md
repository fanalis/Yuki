# Yuki

Yuki is a manga downloader from mangastream.

## Requirements
- Jsoup
- Javatuples
 
Gradle will handle these for you.

## Example usage

```java
import moe.fanalis.yuki.Yuki;

public class Example{
    public static void main(String[] arg){
        Yuki yuuki = new Yuki();
        try{
            yuuki.downloadLastEpisode("Shokugeki no Souma","output");    
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        
    }
}
```
That will create a folder in `./output/shokugeki_no_souma/last_episode_number/` and will download all images there.

## TODO
- GUI

## Advice
This is a "for fun" project. I would recommend to read the manga from mangastream pages and/or buy it.
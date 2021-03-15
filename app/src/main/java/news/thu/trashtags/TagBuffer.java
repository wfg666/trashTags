package news.thu.trashtags;

import java.util.Random;

import static java.lang.Math.abs;

public class TagBuffer {
    protected Tag tags[];

    public TagBuffer(){
        this.tags = new Tag[10];
        for(int i=0;i<tags.length;i++){
            tags[i] = new Tag(new Random().nextInt(), new Random().nextInt(), abs(new Random().nextInt())%100 );
        }
    }

    public Tag[] update(Tag tags[]){
        return this.tags;
    }
}

package news.thu.trashtags;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

public class R2000 {
    public static Tag[] bytesToTags(byte resp[], int len){
        int num = len / 24;
        Tag[] tags = new Tag[num];
        for(int i=0;i<num;i++){
            byte[] high = Arrays.copyOfRange(resp, 8+i*24,12+i*24);
            byte[] low = Arrays.copyOfRange(resp, 12+i*24,20+i*24);
            int epcH = ByteBuffer.wrap(high).getInt();
            long epcL = ByteBuffer.wrap(low).getLong();
            int power = (resp[5+i*24]+60)*2;
            if(power<0)
                power=0;
            if(power>100)
                power=100;
            tags[i] = new Tag(epcH,epcL,power);
        }
        return tags;
    }
}

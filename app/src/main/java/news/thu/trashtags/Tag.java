package news.thu.trashtags;

import android.annotation.SuppressLint;

public class Tag {
    public int epcH;
    public long epcL;
    public int power;

    public Tag(int epcH, long epcL, int power) {
        this.epcH = epcH;
        this.epcL = epcL;
        this.power = power;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("%08X%016X %d", epcH, epcL, power);
    }
}

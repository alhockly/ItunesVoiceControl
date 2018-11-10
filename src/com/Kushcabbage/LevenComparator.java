package com.Kushcabbage;

import java.util.Comparator;

public class LevenComparator implements Comparator<Song> {




    @Override
    public int compare(Song o1, Song o2) {
        if(o1.score<o2.score){
            return -1;
        }
        if(o1.score>o2.score){
            return 1;
        }
        else{
            return 0;
        }
    }
}

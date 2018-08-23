package net.aooms.core.configuration;

/**
 * 常量声明
 * Created by cccyb on 2018-04-20
 */
public interface Vars {

    public static final String ENCODE = "UTF-8";
    public static final String ID = "id";


    public static interface Result{

        public static final String META = "$";
        public static final String DATA = "$data";
        public static final String TOTAL = "$total";
        public static final String RECORD = "$record";

    }

    public static interface Status{
        public static final int YES_FOR_INT = 1;
        public static final int NO_FOR_INT = -1;

        public static final int YES_FOR_CHAR = 'Y';
        public static final int NO_FOR_CHAR = 'N';
    }

}
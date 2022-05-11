package com.konai.appmeter.driver.struct;

import java.text.DecimalFormat;

public class GetDecimalForm {

    DecimalFormat format;

     /**

     *

     */

    public GetDecimalForm() {

        super();

        format = new DecimalFormat("###,###");
    }

    public String getFormat(int value) {

        return format.format(value);

    }

}

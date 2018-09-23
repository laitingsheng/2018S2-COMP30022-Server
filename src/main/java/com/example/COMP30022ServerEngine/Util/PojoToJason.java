package com.example.COMP30022ServerEngine.Util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PojoToJason {
    private static final Logger LOGGER = Logger.getLogger(PojoToJason.class.getName());
    public static String convert(Object ob){
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try{
            String json = mapper.writeValueAsString(ob);
            return json;
        } catch (Exception e){
            LOGGER.log(Level.WARNING, e.toString(), e);
            return null;
        }
    }
}

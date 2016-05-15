package com.company;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {

        BigInteger m1 = new BigInteger("20");
        BigInteger m2 = new BigInteger("2");
        BigInteger m3 = new BigInteger("3");
        BigInteger m4 = new BigInteger("4");
        BigInteger m5 = new BigInteger("5");
        BigInteger m6 = new BigInteger("6");
        BigInteger m7 = new BigInteger("7");

        BigInteger[] key = Rabin.GenKey(100);
        BigInteger n = key[0].multiply(key[1]);

        try {
            BigInteger[] ciphers = Rabin.encrypt(new BigInteger[]{m1,m2,m3,m4,m5,m6,m7
            },n);

            BigInteger[][] m = Rabin.decrypt(ciphers,key);

            for(int i=0;i<m.length;i++){
                System.out.println(i+" : "+m[i][0]+" "+m[i][1]+" "+m[i][2]+" "+m[i][3]+" ");
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //ExtendedEuclid(11,7,77,1,1);

    }







}

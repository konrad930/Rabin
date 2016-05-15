package com.company;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by kgb on 07.05.2016.
 */
public class Rabin {

    public static BigInteger[] GenKey(int keyLength) {

        BigInteger p = genratePrime(keyLength);
        BigInteger q = genratePrime(keyLength);

        return new BigInteger[]{p,q};
    }

    private static BigInteger genratePrime(int keyLength){
        BigInteger n;
        do {
            n = BigInteger.probablePrime(keyLength, new Random());
        } while (!n.mod(new BigInteger("4")).equals(new BigInteger("1")));

        return n;
    }

    //region Encryption

    private static BigInteger encrypt(BigInteger m,BigInteger publicKey){
        return (m.multiply(m)).mod(publicKey);
    }

    public static BigInteger[] encrypt(BigInteger[]messages,BigInteger publicKey)
            throws ExecutionException, InterruptedException {

        BigInteger[] cipher = new BigInteger[messages.length];
        Future[] futures = new Future[messages.length];

        ExecutorService executor = Executors.newFixedThreadPool(10);

        int i = 0;
        for(BigInteger message:messages){
            futures[i] = executor.submit(() -> encrypt(message,publicKey));
            i++;
        }

        for(int j=0;j<cipher.length;j++){
            cipher[j] = (BigInteger) futures[j].get();
        }
        return cipher;
    }

    //endregion

    //region Decryption

    private static BigInteger getSquareRoots(BigInteger cipher,BigInteger key){

        BigInteger p,a,m,b,z,y,x;

        z = new BigInteger("1");
        p = key;

        while(!z.modPow(p.subtract(new BigInteger("1")).divide(new BigInteger("2")),p)
                .equals(p.subtract(new BigInteger("1"))))
        {
            z = z.add(new BigInteger("1"));
        }

        y = cipher;
        m = p.subtract(new BigInteger("1"));

        int s = 0;
        while(m.mod(new BigInteger("2")).equals(new BigInteger("0")) &&
                m.compareTo(new BigInteger("0")) == 1)
        {
            m = m.shiftRight(1);
            s++;
        }

        BigInteger index = m.add(new BigInteger("1")).divide(new BigInteger("2"));

        x = y.modPow(index,p);
        a = y.modPow(m,p);
        b = z.modPow(m,p);

        for(int i=s-1;i>0;i--){
            BigInteger j = new BigInteger("2").pow(i-1);

            if(a.modPow(j,p).equals(new BigInteger("-1").mod(p))){
                a = a.multiply(b.pow(2));
                x = x.multiply(b);
            }

            b=b.modPow(new BigInteger("2"),p);
        }

        return x.mod(p);
    }

    private static BigInteger[] decrypt(BigInteger cipher,BigInteger[] keys,BigInteger yp,BigInteger yq,BigInteger n)
            throws ExecutionException, InterruptedException {

        BigInteger[] messages = new BigInteger[4];
        Future[] fsq = new Future[2];
        BigInteger[] sq = new BigInteger[2];

        ExecutorService executor = Executors.newFixedThreadPool(10);

        int i=0;
        for(BigInteger key:keys){
            fsq[i] = executor.submit(() -> getSquareRoots(cipher,key));
            i++;
        }
        for(int j=0;j<2;j++){
            sq[j] = (BigInteger) fsq[j].get();
        }

        BigInteger r1 = yp.multiply(keys[0]).multiply(sq[1]);
        BigInteger r2 = yq.multiply(keys[1]).multiply(sq[0]);

        messages[0] = (r1.add(r2)).mod(n);
        messages[1] = (n.subtract(messages[0])).mod(n);
        messages[2] = (r1.subtract(r2)).mod(n);
        messages[3] = (n.subtract(messages[2])).mod(n);

        return messages;
    }

    public static BigInteger[][] decrypt(BigInteger[] ciphers,BigInteger keys[])
            throws ExecutionException, InterruptedException {

        BigInteger n = keys[0].multiply(keys[1]);
        BigInteger[] y = ExtendedEuclid(keys[0],keys[1]);
        BigInteger[][] messages = new BigInteger[ciphers.length][];
        Future[] futures = new Future[ciphers.length];
        ExecutorService executor = Executors.newFixedThreadPool(10);

        int i=0;
        for(BigInteger cipher:ciphers){
            futures[i] = executor.submit(() -> decrypt(cipher,keys,y[0],y[1],n));
            i++;
        }
        for(int j=0;j<messages.length;j++){
            messages[j] = (BigInteger[]) futures[j].get();
        }
        return messages;
    }



    private  static  BigInteger[] ExtendedEuclid(BigInteger a, BigInteger b)
    {
        BigInteger q, r, x1, x2, y1, y2;
        BigInteger x = new BigInteger("1");
        BigInteger y = new BigInteger("0");

        if (b.equals(new BigInteger("0")))
        {
            x = new BigInteger("1");
            y = new BigInteger("0");
            return new BigInteger[]{x,y};
        }

        x2 = new BigInteger("1"); x1 = new BigInteger("0");
        y2 = new BigInteger("0"); y1 = new BigInteger("1");

        while (b.compareTo(new BigInteger("0"))==1)
        {
            q = a.divide(b); r = a .subtract(q.multiply(b));
            x = x2.subtract(q.multiply(x1));
            y = y2.subtract(q.multiply(y1));
            a = b; b = r;
            x2 = x1; x1 = x; y2 = y1; y1 = y;
        }

        x= x2;
        y= y2;

        return new BigInteger[]{x,y};
    }

    //endregion
}


package mx.softel.cirwirelesslib.utils;


public class CLibraryWrapper {

    static {
        System.loadLibrary("SG4");
    }


    public native void  SG4_enc(byte[] inDiv, int inDiversifierSize, byte[] ioData, int inDataSize);
    public native void  SG4_dec(byte[] inDiv, int inDiversifierSize, byte[] ioData, int inDataSize);
    public native float SG4_probe_read_temp(int raw);


    public byte[] getEnc(byte[] mac, byte[] data, int encriptacion) {
        byte[] macRevers = new byte[]{(byte) mac[5], (byte) mac[4], (byte) mac[3], (byte) mac[2], (byte) mac[1], (byte) mac[0]};
        switch (encriptacion) {
            case 1:
                this.SG4_enc(macRevers, macRevers.length, data, data.length);
                break;
        }
        return data;
    }

    public byte[] getDec(byte[] mac, byte[] data, byte[] llaveSTC) {
        byte[] macRevers = new byte[]{(byte) mac[5], (byte) mac[4], (byte) mac[3], (byte) mac[2], (byte) mac[1], (byte) mac[0]};
        this.SG4_dec(macRevers, macRevers.length, data, data.length);
        return data;
    }


    public byte[] getBeaconDecodeFuncionEncript(byte[] mac, byte[] beacon, int encriptacion) {
        byte[] macRevers = new byte[]{(byte) mac[5], (byte) mac[4], (byte) mac[3], (byte) mac[2], (byte) mac[1], (byte) mac[0]};
        byte[] avd = new byte[22];
        byte[] scr = new byte[27];
        int i = 0, j = 0;
        for (i = 9, j = 0; i < 31; i++, j++) {
            avd[j] = beacon[i];
        }
        for (i = 35, j = 0; i < beacon.length; i++, j++) {
            scr[j] = beacon[i];
        }

        //50ba963cd40178196b86dfe36506c36d2dcea1fcfc81f7a04f5846
        //50ba963cd40178196b86dfe36506c36d2dcea1fcfc81f7a04f58
        //Log.e("scr",Utils.getHexValue(scr));
        switch (encriptacion) {
            case 1:
                this.SG4_dec(macRevers, macRevers.length, avd, avd.length);
                this.SG4_dec(macRevers, macRevers.length, scr, scr.length);
                break;
        }


        byte[] beaconDecode = new byte[beacon.length];

        for (i = 0; i < 9; i++) {
            beaconDecode[i] = beacon[i];
        }


        for (j = 0; j < avd.length; j++, i++) {
            beaconDecode[i] = avd[j];
        }

        for (j = 31; j < 35; j++, i++) {
            beaconDecode[i] = beacon[j];
        }


        for (j = 0; j < scr.length; i++, j++) {
            beaconDecode[i] = scr[j];
        }

        //beaconDecode[i]=beacon[beacon.length-1];

        //Log.e(" deco",Utils.getHexValue(beaconDecode));

        return beaconDecode;

    }

    public float getTempADC(int valorAdc) {
        return this.SG4_probe_read_temp(valorAdc);
    }


}
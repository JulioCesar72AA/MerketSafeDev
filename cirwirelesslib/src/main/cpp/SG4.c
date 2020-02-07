/*
Spirtech Grieu 4 (SG4)

SG4 is a fast symmetric cipher that reversibly encrypts/decrypts a message of 2 to 255 bytes,
using a 128 bits key, and a key diversifier (the card or ticket identifier), the ciphered
message being the concatenation of data fields stored in the card or ticket.

SG4 is:
- concise,
- portable,
- fast (optimized for 8 bit processors),
- proprietary,
- believed secure under the assumption that adversary:
   - cannot change inKey, inDiversifierSize, inDataSize
	 - can mount chosen-plaintext attacks on inDiv and ioData.
*/

#include <jni.h>
#include  <limits.h>

/**************************************************************************
    ENCRIPTA
***************************************************************************/

#define kSG4KeyRounds (6*16) /* number of iteration for key diversification */
#define kSG4DtaRounds 12     /* number of iteration on each data byte */

typedef unsigned char tByte;

#if UCHAR_MAX != 255
#error /* tByte must be 8 bits */
#endif


tByte llaveSTC[] = {"+rJ/HlGVZrvzm*@$"};

/**************************************************************************
    ENCRIPTA
***************************************************************************/

/**************************************************************************
    Ctes para tabla de temperaturas para NTCs
***************************************************************************/

#define THERM_POINTS        18          //Número de datos en la tabla de resistencias del termistor
#define THERM_TEMP0         -350        //Temperatura del primer elemento (multiplicada por 10)
#define THERM_TEMP_STEP     50          //Diferencia de temperatura entre cada elemento de la tabla (multiplicada por 10)
#define R_SER_NTC           10000UL     //valor de resistencia en serie con el termistor
#define TEMP_MIN_ESC        THERM_TEMP0                                     //minima temperatura que puede medirse
#define TEMP_MAX_ESC        (THERM_TEMP0+(THERM_POINTS-1)*THERM_TEMP_STEP)  //maxima temperatura que puede medirse

#define ADC_RESOLUTION      10U    //La resolución del ADC es de 10 bits
#define ADC_MAX          	((1U<<ADC_RESOLUTION)-1)


/******************************************************************************
    Tabla de resistencia del NTC
*******************************************************************************/
const uint32_t therm_res[THERM_POINTS]={121800,88766,65333,48614,36503,27680,21166,16330,12696,9951,7855,6246,5000,4029,3266,2665,2186,1803};

/******************************************************************************
    Funciones locales.
    El argumento raw es el valor ADC/4
*******************************************************************************/
uint32_t get_rtherm(uint8_t raw);
float probe_read_temp(uint8_t raw);


/**************************************************************************
    ENCRIPTA
***************************************************************************/

JNIEXPORT void JNICALL


Java_mx_softel_cirwirelesslib_utils_CLibraryWrapper_SG4_1enc(
        JNIEnv *env, jobject instance,  jbyteArray inDiv_, jint inDiversifierSize,
        jbyteArray ioData_, jint inDataSize) {

    jbyte *inDiv = (*env)->GetByteArrayElements(env, inDiv_, NULL);
    jbyte *ioData = (*env)->GetByteArrayElements(env, ioData_, NULL);

    tByte vDivKey[32]; /* diversified key */
    tByte a;
    tByte j;
    tByte p;
    tByte q;

    /* ---------------------------    */
    /* prepare the diversified key    */
    /* ---------------------------    */
    p = 16;

    /* spread inKey in vDivKey, in such a way that future interaction with inKey are with distant bytes */
    do
    {
        --p;
        vDivKey[p^2] = vDivKey[p+16] = llaveSTC[p^9];
    }
    while (p);

    a = inDataSize;

    /* extra safety: diversification changes with inDataSize */
    p = (q = inDiversifierSize) + kSG4KeyRounds;

    /* for each byte in inDiv, then kSG4KeyRounds extra steps */
    do
    {
        --p;
        vDivKey[p&31] += a ^= (((a>>1)+(q?inDiv[--q]:p))^llaveSTC[p&15])+vDivKey[(p+17)&31];
    }
    while (p||q);

    /* ---------------------------    */
    /* encipher the data              */
    /* ---------------------------    */
    j = inDataSize >> 1;
    a = ioData[inDataSize-1];
    p = 0;

    do
    {
        q = 0;
        do
        {
            a = ioData[q] ^= (((a>>4)+ioData[j])^vDivKey[p&31])+a;
            if ((++j)==inDataSize)
            {
                j=0;
            }
            ++p;
        }
        while(++q!=inDataSize);
        /* inDataSize iterations */
        p -= q;
    }
    while ((p += 13)!=(tByte)(kSG4DtaRounds*13));
    /* kSG4DtaRounds iterations */

    (*env)->ReleaseByteArrayElements(env, inDiv_, inDiv, 0);
    (*env)->ReleaseByteArrayElements(env, ioData_, ioData, 0);
}


JNIEXPORT void JNICALL
Java_mx_softel_cirwirelesslib_utils_CLibraryWrapper_SG4_1dec(JNIEnv *env, jobject instance,
                                                             jbyteArray inDiv_,
                                                             jint inDiversifierSize,
                                                             jbyteArray ioData_, jint inDataSize) {

    jbyte *inDiv = (*env)->GetByteArrayElements(env, inDiv_, NULL);
    jbyte *ioData = (*env)->GetByteArrayElements(env, ioData_, NULL);

    tByte	vDivKey[32]; /* diversified key */
    tByte a;
    tByte j;
    tByte p;
    tByte q;

    /* ---------------------------    */
    /* prepare the diversified key    */
    /* ---------------------------    */
    p = 16;

    /* spread inKey in vDivKey, in such a way that future interaction with inKey are with distant bytes */
    do
    {
        --p;
        vDivKey[p^2] = vDivKey[p+16] = llaveSTC[p^9];
    }
    while (p);

    a = inDataSize;

    /* extra safety: diversification changes with inDataSize */
    p = (q = inDiversifierSize) + kSG4KeyRounds;

    /* for each byte in inDiv, then kSG4KeyRounds extra steps */
    do
    {
        --p;
        vDivKey[p&31] += a ^= (((a>>1)+(q?inDiv[--q]:p))^llaveSTC[p&15])+vDivKey[(p+17)&31];
    }
    while (p||q);

    /* ---------------------------    */
    /* decipher the data              */
    /* ---------------------------    */
    j = inDataSize >> 1;
    p = (tByte)(kSG4DtaRounds*13);
    /* kSG4DtaRounds iterations */

    do
    {
        p += (q = inDataSize)-13;
        /* inDataSize iterations */
        do
        {
            if (j==0)
            {
                j = inDataSize;
            }
            if ((a = --q)==0)
            {
                a = inDataSize;
            }
            a = ioData[a-1];
            ioData[q] ^= (((a>>4)+ioData[--j])^vDivKey[(--p)&31])+a;
        }
        while (q);
    }
    while (p);


    (*env)->ReleaseByteArrayElements(env, inDiv_, inDiv, 0);
    (*env)->ReleaseByteArrayElements(env, ioData_, ioData, 0);
}

/**************************************************************************
    Ctes para tabla de temperaturas para NTCs
***************************************************************************/



uint32_t get_rtherm(uint8_t raw){
    uint32_t rtherm;
    uint16_t ntherm;
    uint32_t rser;


    ntherm=raw*4;   //reescala al avlor leido por el ADC
    rser=R_SER_NTC;

    //ecuacion independiente del voltaje de referencia y del voltaje del termistor
    rtherm=ntherm*rser;
    rtherm/=ADC_MAX-ntherm; //calcula resistencia termistor
    //Las operaciones se hacen por partes para que el compilador
    //no marque error: fixup overflow referecing psect dataBANK

    return rtherm;

}




JNIEXPORT jfloat JNICALL
Java_mx_softel_cirwirelesslib_utils_CLibraryWrapper_SG4_1probe_1read_1temp(JNIEnv *env,
                                                                           jobject instance,
                                                                           jint raw){


    uint32_t rtherm;
    uint32_t res1, res2;
    int32_t aux;
    int32_t  temp,temp2;
    uint8_t i=0;

    rtherm=get_rtherm(raw);


    //checa si el valor está fuera de la tabla
    if(rtherm>therm_res[0])
        temp=TEMP_MIN_ESC;
    else if(rtherm<therm_res[THERM_POINTS-1])
        temp=TEMP_MAX_ESC;
    else{
        //busca indice en arreglo
        for(i=0;(i<THERM_POINTS)&&(rtherm<therm_res[i]);i++);

        //calcula temperatura por interpolación lineal
        //T=THERM_TEMP_STEP*(R2-RT)/(R2-R1)+T2
        res1=therm_res[i];
        res2=therm_res[i-1];

        temp2=(i-1)*THERM_TEMP_STEP;
        temp2+=THERM_TEMP0;

        aux=res2-rtherm;
        aux*=THERM_TEMP_STEP;
        aux/=(res2-res1);       //Las operaciones se hacen por partes para que el compilador
        //no marque error: fixup overflow referecing psect dataBANK
        temp=aux+temp2;

    }

    return ((float)temp/10);
}


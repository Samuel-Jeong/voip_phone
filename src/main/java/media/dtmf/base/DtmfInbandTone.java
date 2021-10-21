package media.dtmf.base;

/*
  Manages the generation of the inband DMTF signal. A signal is identified by a
  value (1, 2, 3, 4, 5, 6, 7, 8, 9, *, #, A, B, C and D) and each signal is
  produced by the composition of 2 frequencies (as defined below).
  (cf. ITU recommendation Q.23)

  +------------------------------------------------+
  |        | 1209 Hz | 1336 Hz | 1477 Hz | 1633 Hz |
  +------------------------------------------------+
  | 697 Hz |    1    |    2    |    3    |    A    |
  | 770 Hz |    4    |    5    |    6    |    B    |
  | 852 Hz |    7    |    8    |    9    |    C    |
  | 941 Hz |    *    |    0    |    #    |    D    |
  +------------------------------------------------+
 */

/**
 * @class public class DtmfInbandTone
 * @brief DtmfInbandTone class
 */
public class DtmfInbandTone
{
    /**
     * The first set of frequencies in Hz which composes an inband DTMF.
     */
    private static final double[] FREQUENCY_LIST_1 = new double[] { 697.0, 770.0, 852.0, 941.0 };

    /**
     * The second set of frequencies in Hz which composes an inband DTMF.
     */
    private static final double[] FREQUENCY_LIST_2 = new double[] { 1209.0, 1336.0, 1477.0, 1633.0 };

    /**
     * The "0" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_0 = new DtmfInbandTone("0",
            FREQUENCY_LIST_1[3],
            FREQUENCY_LIST_2[1]);

    /**
     * The "1" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_1 = new DtmfInbandTone("1",
            FREQUENCY_LIST_1[0],
            FREQUENCY_LIST_2[0]);

    /**
     * The "2" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_2 = new DtmfInbandTone("2",
            FREQUENCY_LIST_1[0],
            FREQUENCY_LIST_2[1]);

    /**
     * The "3" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_3 = new DtmfInbandTone("3",
            FREQUENCY_LIST_1[0],
            FREQUENCY_LIST_2[2]);

    /**
     * The "4" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_4 = new DtmfInbandTone("4",
            FREQUENCY_LIST_1[1],
            FREQUENCY_LIST_2[0]);

    /**
     * The "5" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_5 = new DtmfInbandTone("5",
            FREQUENCY_LIST_1[1],
            FREQUENCY_LIST_2[1]);

    /**
     * The "6" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_6 = new DtmfInbandTone("6",
            FREQUENCY_LIST_1[1],
            FREQUENCY_LIST_2[2]);

    /**
     * The "7" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_7 = new DtmfInbandTone("7",
            FREQUENCY_LIST_1[2],
            FREQUENCY_LIST_2[0]);

    /**
     * The "8" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_8 = new DtmfInbandTone("8",
            FREQUENCY_LIST_1[2],
            FREQUENCY_LIST_2[1]);

    /**
     * The "9" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_9 = new DtmfInbandTone("9",
            FREQUENCY_LIST_1[2],
            FREQUENCY_LIST_2[2]);

    /**
     * The "*" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_STAR =
            new DtmfInbandTone("*",
                    FREQUENCY_LIST_1[3],
                    FREQUENCY_LIST_2[0]);

    /**
     * The "#" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_SHARP =
            new DtmfInbandTone("#",
                    FREQUENCY_LIST_1[3],
                    FREQUENCY_LIST_2[2]);

    /**
     * The "A" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_A = new DtmfInbandTone("A",
            FREQUENCY_LIST_1[0],
            FREQUENCY_LIST_2[3]);

    /**
     * The "B" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_B = new DtmfInbandTone("B",
            FREQUENCY_LIST_1[1],
            FREQUENCY_LIST_2[3]);

    /**
     * The "C" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_C = new DtmfInbandTone("C",
            FREQUENCY_LIST_1[2],
            FREQUENCY_LIST_2[3]);

    /**
     * The "D" DTMF Inband Tone.
     */
    public static final DtmfInbandTone DTMF_INBAND_D = new DtmfInbandTone("D",
            FREQUENCY_LIST_1[3],
            FREQUENCY_LIST_2[3]);

    /**
     * The default duration of an inband DTMF tone in ms.
     * 50 ms c.f.
     * http://nemesis.lonestar.org/reference/telecom/signaling/dtmf.html
     * which cites the norm ANSI T1.401-1988.
     * But when testing it at 50 ms, the Asterisk servers miss some DTMF tone
     * impulses. Thus, set up to 150 ms.
     */
    private static final int TONE_DURATION = 150;

    /**
     * The default duration of an inband DTMF tone in ms.
     * 45 ms c.f.
     * http://nemesis.lonestar.org/reference/telecom/signaling/dtmf.html
     * which cites the norm ANSI T1.401-1988.
     * Moreover, the minimum duty cycle (signal tone + silence) for
     * ANSI-compliance shall be greater or equal to 100 ms.
     */
    private static final int INTER_DIGIT_INTERVAL = 45;

    /**
     * The value which identifies the current inband tone. Available values are
     * (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, *, #, A, B, C and D).
     */
    private final String value;

    /**
     * The first frequency which composes the current inband tone.
     */
    private final double frequency1;

    /**
     * The second frequency which composes the current inband tone.
     */
    private final double frequency2;

    /**
     * Creates a new instance of an inband tone. The value given is the main
     * identifier which determines which are the two frequencies to used to
     * generate this tone.
     *
     * @param value The identifier of the tone. Available values are (0, 1, 2,
     * 3, 4, 5, 6, 7, 8, 9, *, #, A, B, C and D).
     * @param frequency1 The first frequency which composes the tone. Available
     * values corresponds to DTMFInbandTone.frequencyList1.
     * @param frequency2 The second frequency which composes the tone. Available
     * values corresponds to DTMFInbandTone.frequencyList2.
     */
    public DtmfInbandTone(String value, double frequency1, double frequency2) {
        this.value = value;
        this.frequency1 = frequency1;
        this.frequency2 = frequency2;
    }

    /**
     * Returns this tone value as a string representation.
     *
     * @return this tone value.
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Returns the first frequency coded by this tone.
     *
     * @return the first frequency coded by this tone.
     */
    public double getFrequency1()
    {
        return this.frequency1;
    }

    /**
     * Returns the second frequency coded by this tone.
     *
     * @return the second frequency coded by this tone.
     */
    public double getFrequency2()
    {
        return this.frequency2;
    }

    /**
     * Generates a sample for the current tone signal.
     *
     * @param samplingFrequency The sampling frequency (codec clock rate) in Hz
     * of the stream which will encapsulate this signal.
     * @param sampleNumber The sample number of this signal to be produced. The
     * sample number corresponds to the abscissa of the signal function.
     *
     * @return the sample generated. This sample corresponds to the ordinate of
     * the signal function
     */
    public double getAudioSampleContinuous(double samplingFrequency, int sampleNumber) {
        double u1 = 2.0 * Math.PI * this.frequency1 / samplingFrequency;
        double u2 = 2.0 * Math.PI * this.frequency2 / samplingFrequency;
        return Math.sin(u1 * sampleNumber) * 0.5 + Math.sin(u2 * sampleNumber) * 0.5;
    }

    /**
     * Generates a sample for the current tone signal converted into a discrete
     * signal.
     *
     * @param samplingFrequency The sampling frequency (codec clock rate) in Hz
     * of the stream which will encapsulate this signal.
     * @param sampleNumber The sample number of this signal to be produced. The
     * sample number corresponds to the abscissa of the signal function.
     * @param sampleSizeInBits The size of each sample (8 for a byte, 16 for a
     * short and 32 for an int)
     *
     * @return the sample generated. This sample corresponds to the ordinate of
     * the signal function
     */
    public int getAudioSampleDiscrete(double samplingFrequency, int sampleNumber, int sampleSizeInBits) {
        // generates a signal between -2147483647 and 2147483647.
        double audioSampleContinuous = getAudioSampleContinuous(samplingFrequency, sampleNumber);
        double amplitudeCoefficient = (1L << (sampleSizeInBits - 1)) - 1L;

        return (int) (audioSampleContinuous * amplitudeCoefficient);
    }

    /**
     * Generates a signal sample for the current tone signal and stores it into
     * the byte data array.
     *
     * @param sampleRate The sampling frequency (codec clock rate) in Hz of the
     * stream which will encapsulate this signal.
     * @param sampleSizeInBits The size of each sample (8 for a byte, 16 for a
     * short and 32 for an int)
     * @return The data array containing the DTMF signal.
     */
    public short[] getAudioSamples(double sampleRate, int sampleSizeInBits) {
        int kHz = (int) (sampleRate / 1000.0);
        int nbToneSamples = kHz * DtmfInbandTone.TONE_DURATION;
        int nbInterDigitSamples = kHz * DtmfInbandTone.INTER_DIGIT_INTERVAL;
        short[] samples = new short[nbInterDigitSamples + nbToneSamples + nbInterDigitSamples];

        for (int sampleNumber = nbInterDigitSamples, endSampleNumber = nbInterDigitSamples + nbToneSamples; sampleNumber < endSampleNumber; sampleNumber++) {
            samples[sampleNumber] = (short) getAudioSampleDiscrete(
                            sampleRate,
                            sampleNumber,
                            sampleSizeInBits
            );
        }

        return samples;
    }

}


/**
 * Translated from the C code of Skype SILK codec (ver. 1.0.6)
 * Downloaded from http://developer.skype.com/silk/
 * 
 * Class "Silk_detect_SWB_input" is mainly based on 
 * ../SILK_SDK_SRC_FLP_v1.0.6/src/SKP_Silk_detect_SWB_input.c
 */
package net.java.sip.communicator.impl.neomedia.codec.audio.silk;

/**
 * @author 
 *
 */
public class Silk_detect_SWB_input 
{
	static void SKP_Silk_detect_SWB_input(
		    SKP_Silk_detect_SWB_state   psSWBdetect,   /* (I/O) encoder state  */
		    short[]             samplesIn,    /* (I) input to encoder */
		    int samplesIn_offset,
		    int                     nSamplesIn      /* (I) length of input */
		)
		{
		    int     HP_8_kHz_len, i, shift;
		    short[]   in_HP_8_kHz = new short[ Silk_define.MAX_FRAME_LENGTH ];
		    int   energy_32;
		    
		    /* High pass filter with cutoff at 8 khz */
		    HP_8_kHz_len = Math.min( nSamplesIn, Silk_define.MAX_FRAME_LENGTH );
		    HP_8_kHz_len = Math.max( HP_8_kHz_len, 0 );

		    /* Cutoff around 9 khz */
		    /* A = conv(conv([8192,14613, 6868], [8192,12883, 7337]), [8192,11586, 7911]); */
		    /* B = conv(conv([575, -948, 575], [575, -221, 575]), [575, 104, 575]); */
		    Silk_biquad.SKP_Silk_biquad( samplesIn, Silk_tables_other.SKP_Silk_SWB_detect_B_HP_Q13[ 0 ], Silk_tables_other.SKP_Silk_SWB_detect_A_HP_Q13[ 0 ], 
		        psSWBdetect.S_HP_8_kHz[ 0 ], in_HP_8_kHz, HP_8_kHz_len );
		    for( i = 1; i < Silk_define.NB_SOS; i++ ) 
		    {
		    	Silk_biquad.SKP_Silk_biquad( in_HP_8_kHz, Silk_tables_other.SKP_Silk_SWB_detect_B_HP_Q13[ i ], Silk_tables_other.SKP_Silk_SWB_detect_A_HP_Q13[ i ], 
		            psSWBdetect.S_HP_8_kHz[ i ], in_HP_8_kHz, HP_8_kHz_len );
		    }

		    /* Calculate energy in HP signal */
		    Silk_sum_sqr_shift.SKP_Silk_sum_sqr_shift( &energy_32, &shift, in_HP_8_kHz, HP_8_kHz_len );

		    /* Count concecutive samples above threshold, after adjusting threshold for number of input samples and shift */
		    if( energy_32 > Silk_macros.SKP_SMULBB( Silk_define.HP_8_KHZ_THRES, HP_8_kHz_len ) >> shift ) 
		    {
		        psSWBdetect.ConsecSmplsAboveThres += nSamplesIn;
		        if( psSWBdetect.ConsecSmplsAboveThres > Silk_define.CONCEC_SWB_SMPLS_THRES )
		        {
		            psSWBdetect.SWB_detected = 1;
		        }
		    } 
		    else
		    {
		        psSWBdetect.ConsecSmplsAboveThres -= nSamplesIn;
		        psSWBdetect.ConsecSmplsAboveThres = Math.max( psSWBdetect.ConsecSmplsAboveThres, 0 );
		    }

		    /* If sufficient speech activity and no SWB detected, we detect the signal as being WB */
		    if( ( psSWBdetect.ActiveSpeech_ms > Silk_define.WB_DETECT_ACTIVE_SPEECH_MS_THRES ) && ( psSWBdetect.SWB_detected == 0 ) ) 
		    {
		        psSWBdetect.WB_detected = 1;
		    }
		}
}




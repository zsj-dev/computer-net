package project01part3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import com.synthbot.jasiohost.*;



public class Sender implements AsioDriverListener {
    private AsioDriver asioDriver;
    private Set<AsioChannel> activeChannels;

    private LinkedList<Float> recording;
    private AsioChannel outputChannel;

    private float phase = 0;

    private float[] output;
    private byte[] fileContent;

    private float dphase_zero = (2 * (float)Math.PI * Config.lowFreq) / Config.PHY_TX_SAMPLING_RATE;
    private float dphase_one = (2 * (float)Math.PI * Config.highFreq) / Config.PHY_TX_SAMPLING_RATE;




    public void init()  {
        activeChannels = new HashSet<AsioChannel>();  // create a Set of AsioChannels

        recording = new LinkedList<>();


        if (asioDriver == null) {
            asioDriver = AsioDriver.getDriver("ASIO4ALL v2");
            asioDriver.addAsioDriverListener(this);   // add an AsioDriverListener in order to receive callbacks from the driver

            outputChannel = asioDriver.getChannelOutput(0);


            output = new float[Config.HW_BUFFER_SIZE];


            asioDriver.setSampleRate(Config.PHY_TX_SAMPLING_RATE);

            activeChannels.add(outputChannel);

            asioDriver.createBuffers(activeChannels);  // create the audio buffers and prepare the driver to run
            System.out.println("ASIO buffer created, size: " + asioDriver.getBufferPreferredSize());

        }
        FileInputStream is = null;
        try {
            is = new FileInputStream("D:\\cs120\\project\\src\\project01part3\\INPUT.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        int avail = 0;
        try {
            avail = is.available();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileContent = new byte[avail];
        try {
            is.read(fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < fileContent.length; i++) {
            if(i % Config.packSize == 0){
                for (int j = 0; j < Config.header.length; j++) {
                    recording.add(Config.header[j]);
                }
            }
            phase = 0f;
            if(fileContent[i]==48){
                for (int j = 0; j < Config.contentLen; j++) {
                    phase= phase+dphase_zero;
                    recording.add((float)(Math.sin((double)phase)));
                }

            }else{
                for (int j = 0; j < Config.contentLen; j++) {
                    phase= phase+dphase_one;
                    recording.add((float)(Math.sin((double)phase)));
                }

            }

        }



    }




    public void start() {
        if (asioDriver != null) {
            asioDriver.start();  // start the driver
            System.out.println(asioDriver.getCurrentState());
        }
    }

    public void stop() {
        asioDriver.returnToState(AsioDriverState.INITIALIZED);
        asioDriver.shutdownAndUnloadDriver();  // tear everything down
    }



    @Override
    public void bufferSwitch(final long systemTime, final long samplePosition, final Set<AsioChannel> channels) {
        for (AsioChannel channelInfo : channels) {
            for (int i = 0; i < Config.HW_BUFFER_SIZE; i++) {
                if(recording.peek()==null){
                    output[i]=0f;
                }else {

                    output[i] = recording.poll();

                }
            }
            channelInfo.write(output);
        }

    }

    @Override
    public void latenciesChanged(final int inputLatency, final int outputLatency) {
        System.out.println("latenciesChanged() callback received.");
    }

    @Override
    public void bufferSizeChanged(final int bufferSize) {
        System.out.println("bufferSizeChanged() callback received.");
    }

    @Override
    public void resetRequest() {
        /*
         * This thread will attempt to shut down the ASIO driver. However, it will block
         * on the AsioDriver object at least until the current method has returned.
         */
        new Thread() {
            @Override
            public void run() {
                System.out.println("resetRequest() callback received. Returning driver to INITIALIZED state.");
                asioDriver.returnToState(AsioDriverState.INITIALIZED);
            }
        }.start();
    }

    @Override
    public void resyncRequest() {
        System.out.println("resyncRequest() callback received.");
    }

    @Override
    public void sampleRateDidChange(final double sampleRate) {
        System.out.println("sampleRateDidChange() callback received.");
    }

    public static void main(String[] args) {
        final Sender sender= new Sender();
        sender.init();
        sender.start();
        try {
            Thread.sleep(15000);  // ms
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        sender.stop();

    }

}

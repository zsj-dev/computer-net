package project01part3;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import com.synthbot.jasiohost.*;

public class Receiver implements AsioDriverListener{
    private AsioDriver asioDriver;
    private Set<AsioChannel> activeChannels;

    private LinkedList<Float> recording;
    private AsioChannel inputChannel;

    private float[] input;

    private float[] inputArray;

    private float[] conv_result;
    public void init()  {
        activeChannels = new HashSet<AsioChannel>();  // create a Set of AsioChannels

        recording = new LinkedList<>();


        if (asioDriver == null) {
            asioDriver = AsioDriver.getDriver("ASIO4ALL v2");
            asioDriver.addAsioDriverListener(this);   // add an AsioDriverListener in order to receive callbacks from the driver

            inputChannel = asioDriver.getChannelInput(0);


            input = new float[Config.HW_BUFFER_SIZE];


            asioDriver.setSampleRate(Config.PHY_TX_SAMPLING_RATE);
            /*
             * buffer size should be set either by modifying the JAsioHost source code or
             * configuring the preferred value in ASIO native window. We choose 128 i.e.,
             * asioDriver.getBufferPreferredSize() should be equal to Config.HW_BUFFER_SIZE
             * = 128;
             *
             */
            activeChannels.add(inputChannel);

            asioDriver.createBuffers(activeChannels);  // create the audio buffers and prepare the driver to run
            System.out.println("ASIO buffer created, size: " + asioDriver.getBufferPreferredSize());

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
            channelInfo.read(input);
            for (int i = 0; i < input.length; i++) {
                recording.add(input[i]);

            }
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

    public void listToArray(){
        int size = recording.size();
        inputArray = new float[size];
        for (int i=0; i<size; i++) {
            inputArray[i]=recording.poll();
        }

    }
    public void conv(){
        int last = 0;
        int count = 0;
        conv_result = new float[inputArray.length-Config.header.length+1];
        for (int i = 0; i < conv_result.length; i++) {
            float temp_sum = 0;
            for (int j = 0; j < Config.header.length; j++) {
                temp_sum = temp_sum+Config.header[j]*inputArray[i+j];
            }
            conv_result[i]=temp_sum;
            if(temp_sum>8){
                if (last==0 || (i-last)>440) {
                    last=i;
                    count++;
                    System.out.println(count + " " + temp_sum + " " + i);
                }

            }

        }

    }


    public static void main(String[] args) {
        final Receiver receiver= new Receiver();
        receiver.init();
        receiver.start();
        try {
            Thread.sleep(13000);  // ms
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        receiver.stop();
        System.out.println("start to change");
        receiver.listToArray();
        receiver.conv();
        System.out.println("end");


    }

}

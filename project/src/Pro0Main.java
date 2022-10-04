public class Pro0Main {

    public void CK1(){
        final AudioHw audiohw = new AudioHw();
        audiohw.init();
        audiohw.setStatus(1);
        audiohw.start();
        try {
            Thread.sleep(10000);  // ms
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        audiohw.setStatus(-1);
        try {
            Thread.sleep(10000);  // ms
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        audiohw.stop();

    }
    public void CK2(){
        final AudioHw audiohw = new AudioHw();
        audiohw.init();
        audiohw.setStatus(0);
        audiohw.insertAudio();
        audiohw.start();
        try {
            Thread.sleep(10000);  // ms
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        audiohw.setStatus(-1);
        try {
            Thread.sleep(10000);  // ms
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        audiohw.stop();

    }


    public void Pro1Part2(){
        final AudioHw audiohw = new AudioHw();
        audiohw.init();
        audiohw.start();
        try {
            Thread.sleep(10000);  // ms
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        audiohw.stop();

    }
    public static void main(final String[] args) {
        final Pro0Main task = new Pro0Main();
        task.Pro1Part2();



    }
}

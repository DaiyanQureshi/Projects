package ultimate_files;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {
    private static AppExecutor instance;
    private final ExecutorService diskIO;
    private final Handler mainThread;

    private AppExecutor() {
        // CPU cores ke hisab se dynamic thread pool
        int cores = Runtime.getRuntime().availableProcessors();
        diskIO = Executors.newFixedThreadPool(cores);
        mainThread = new Handler(Looper.getMainLooper());
    }

    public static synchronized AppExecutor getInstance() {
        if (instance == null) {
            instance = new AppExecutor();
        }
        return instance;
    }

    public ExecutorService diskIO() { return diskIO; }
    public Handler mainThread() { return mainThread; }
}
package name.remal.gradle_plugins.merge_resources.mergers;

import static com.google.common.jimfs.Configuration.unix;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.FileSystem;
import javax.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.parallel.Execution;

@Execution(SAME_THREAD)
abstract class JimTestUtils {

    protected static FileSystem getFilesSystem() {
        FileSystem fs = currentFileSystem.get();
        if (fs == null) {
            currentFileSystem.set(fs = Jimfs.newFileSystem(unix()));
        }
        return fs;
    }

    @AfterEach
    void closeCurrentFilesSystem() throws Throwable {
        var fs = currentFileSystem.get();
        if (fs != null) {
            fs.close();
        }
        currentFileSystem.remove();
    }


    //#region In-memory file system support

    private static final String CURRENT_FILE_SYSTEM_URL_PROTOCOL = "current-fs";

    private static final ThreadLocal<FileSystem> currentFileSystem = new ThreadLocal<>();

    static {
        URL.setURLStreamHandlerFactory(new CurrentFileSystemUrlStreamHandlerFactory());
    }


    private static class CurrentFileSystemUrlStreamHandlerFactory implements URLStreamHandlerFactory {
        @Nullable
        @Override
        public URLStreamHandler createURLStreamHandler(@Nullable String protocol) {
            if (CURRENT_FILE_SYSTEM_URL_PROTOCOL.equals(protocol)) {
                return new CurrentUrlStreamHandler();
            }

            return null;
        }

    }

    private static class CurrentUrlStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL url) {
            return new CurrentUrlConnection(url);
        }
    }

    private static class CurrentUrlConnection extends URLConnection {

        public CurrentUrlConnection(URL url) {
            super(url);
        }

        @Nullable
        private InputStream stream;

        @Override
        public void connect() throws IOException {
            if (stream != null) {
                return;
            }

            var path = currentFileSystem.get().getPath(url.getPath());
            if (isDirectory(path)) {
                throw new AssertionError("A directory: " + path);
            }

            this.stream = newInputStream(path);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return requireNonNull(stream);
        }

    }

    //#endregion

}

package yap.memtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public final class MainActivity extends Activity {
    private TextView view;
    private Toast toast;
    private final int KB_TO_EAT = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new TextView(this);
        view.setTextSize(24f);
        view.setPadding(8, 8, 8, 8);
        view.setOnClickListener(this::onClick);
        view.setOnLongClickListener(this::onLongClick);
        setContentView(view);

        toast = Toast.makeText(this, null, 0);

        cleanFilesDir();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showMem();
    }

    private void onClick(View unused) {
        eat(KB_TO_EAT);
        showMem();
    }

    private boolean onLongClick(View unused) {
        free();
        showMem();
        return true;
    }

    private String readMemInfo() {
        StringBuilder mem = new StringBuilder();
        File meminfo = new File("/proc/meminfo");
        try (Scanner sc = new Scanner(meminfo)) {
            while(sc.hasNextLine()) {
                String s = sc.nextLine();
                if (s.startsWith("MemFree") || s.startsWith("MemAvailable") || s.startsWith("Cached") || s.startsWith("Swap")) {
                    mem.append(s);
                    mem.append("\n");
                }
            }
        } catch (IOException e) {
            return e.getMessage();
        }
        return mem.toString();
    };

    private void cleanFilesDir() {
        for (File f : getFilesDir().listFiles())
            f.delete();
    }

    private String readFilesDir() {
        StringBuilder files = new StringBuilder("Content of files dir:\n");
        for (File f : getFilesDir().listFiles()) {
            files.append(f.getName());
            files.append(" ");
            files.append(f.length());
            files.append("\n");
        }
        return files.toString();
    }

    private void showMem() {
        StringBuilder text = new StringBuilder("This is a test memory eater\n");
        text.append("Usage:\n");
        text.append("Tap to eat " + KB_TO_EAT + " kB\n");
        text.append("Long tap to free " + KB_TO_EAT + " kB\n");
        text.append("----------\n");
        text.append(readMemInfo());
        text.append("----------\n");
        text.append(readFilesDir());
        text.append("\n");
        view.setText(text);
    }

    private void eat(int kB) {
        File random = new File("/dev/zero");
        File f = new File(getFilesDir(), "" + System.currentTimeMillis());
        byte[] buf = new byte[1024];
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(random));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {
            for (int i = 0; i < kB; i++) {
                int n = is.read(buf, 0, 1024);
                if (n != 1024)
                    throw new IOException("expected 1024, read " + n);
                os.write(buf, 0, 1024);
            }
            toast(kB + " kB eaten!");
        } catch (IOException e) {
            toast(e.toString());
        }
    }

    private void free() {
        for (File f : getFilesDir().listFiles()) {
            toast("freeing " + f.length() + " bytes");
            f.delete();
            return;
        }
        toast("nothing to free");
    }

    private void toast(String text) {
        toast.setText(text);
        toast.show();
    }
}

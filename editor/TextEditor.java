package editor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

    String previousValue = "";

    public TextEditor() {
        super("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 2000);
        initComponents();
        setVisible(true);
        setLocationRelativeTo(null);
        pack();
    }

    public void initComponents() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenu searchMenu = new JMenu("Search");
        JPanel panel = new JPanel();
        JTextArea textArea = new JTextArea(10, 20);
        JTextField searchField = new JTextField(20);
        JFileChooser jfc = new JFileChooser();
        JScrollPane scrollPane = new JScrollPane(textArea);
        AtomicBoolean foundMatch = new AtomicBoolean(false);
        List<ListIterator<String>> box = new ArrayList<>();

        Icon  saveIcon = new ImageIcon ("Text Editor (1)\\icons\\save.jpg",
                "SaveButton");
        Icon  loadIcon = new ImageIcon ("Text Editor (1)\\icons\\open.jpg",
                "OpenButton");
        Icon  searchIcon = new ImageIcon ("Text Editor (1)\\icons\\search.jpg",
                "StartSearchButton");
        Icon  backIcon = new ImageIcon ("Text Editor (1)\\icons\\back.jpg",
                "PreviousMatchButton");
        Icon  nextIcon = new ImageIcon ("Text Editor (1)\\icons\\next.jpg",
                "NextMatchButton");

        JButton saveButton = new JButton(saveIcon);
        JButton loadButton = new JButton(loadIcon);
        JButton searchButton = new JButton(searchIcon);
        JButton backButton = new JButton(backIcon);
        JButton nextButton = new JButton(nextIcon);
        JCheckBox regexCheckBox = new JCheckBox();
        JLabel regexLabel = new JLabel("Use regex");
        JMenuItem searchItem = new JMenuItem("Search");
        JMenuItem backItem = new JMenuItem("Back");
        JMenuItem nextItem = new JMenuItem("Next");
        JMenuItem useRegexItem = new JMenuItem("Use RegExp");
        JMenuItem loadItem = new JMenuItem("Load");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        // Set widget names
        textArea.setName("TextArea");
        searchField.setName("SearchField");
        saveButton.setName("SaveButton");
        loadButton.setName("OpenButton");
        searchButton.setName("StartSearchButton");
        backButton.setName("PreviousMatchButton");
        nextButton.setName("NextMatchButton");
        regexCheckBox.setName("UseRegExCheckbox");
        jfc.setName("FileChooser");
        scrollPane.setName("ScrollPane");
        menu.setName("MenuFile");
        searchMenu.setName("MenuSearch");
        loadItem.setName("MenuOpen");
        saveItem.setName("MenuSave");
        exitItem.setName("MenuExit");
        searchItem.setName("MenuStartSearch");
        backItem.setName("MenuPreviousMatch");
        nextItem.setName("MenuNextMatch");
        useRegexItem.setName("MenuUseRegExp");


        getContentPane().add(BorderLayout.NORTH, panel);
        panel.setBorder(new EmptyBorder(new Insets(5,5,5,5)));

        saveItem.addActionListener(actionEvent -> saveFile(jfc, textArea));
        saveButton.addActionListener(actionEvent -> saveFile(jfc, textArea));

        loadItem.addActionListener(actionEvent -> loadFile(jfc, textArea));
        loadButton.addActionListener(actionEvent -> loadFile(jfc, textArea));

        searchItem.addActionListener(actionEvent -> {
            ListIterator<String> iterator = findMatches(textArea, searchField, regexCheckBox.isSelected());
            System.out.println(iterator);
            var worker = searchText(textArea, iterator);
            worker.execute();
            try {
                foundMatch.set(worker.get());
                if (foundMatch.get()) {
                    box.clear();
                    box.add(iterator);
                } else {
                    box.clear();
                }
            } catch (Exception e) {
                System.out.println("An error occurred");
            }
        });
        searchButton.addActionListener(actionEvent -> {
            box.clear();
            ListIterator<String> iterator = findMatches(textArea, searchField, regexCheckBox.isSelected());
            var worker = searchText(textArea, iterator);
            worker.execute();

            try {
                foundMatch.set(worker.get());
                if (foundMatch.get()) {
                    box.add(iterator);
                }
            } catch (Exception e) {
                System.out.println("An error occurred");
                e.printStackTrace();
            }
        });

        nextButton.addActionListener(actionEvent -> {
            if (foundMatch.get() && !box.isEmpty()) {
                var iterator = box.get(0);
                var worker = changeMatch(textArea, iterator, true);
                worker.execute();
            }
        });
        nextItem.addActionListener(actionEvent -> {
            if (foundMatch.get() && !box.isEmpty()) {
                var iterator = box.get(0);
                var worker = changeMatch(textArea, iterator, true);
                worker.execute();
            }
        });

        backButton.addActionListener(actionEvent -> {
            if (foundMatch.get() && !box.isEmpty()) {
                var iterator = box.get(0);
                var worker = changeMatch(textArea, iterator, false);
                worker.execute();
            }
        });
        backItem.addActionListener(actionEvent -> {
            if (foundMatch.get() && !box.isEmpty()) {
                var iterator = box.get(0);
                var worker = changeMatch(textArea, iterator, false);
                worker.execute();
            }
        });

        useRegexItem.addActionListener(actionEvent -> regexCheckBox.setSelected(!regexCheckBox.isSelected()));
        exitItem.addActionListener(actionEvent -> dispose());

        saveButton.setPreferredSize(new Dimension(28, 28));
        loadButton.setPreferredSize(new Dimension(28, 28));
        searchButton.setPreferredSize(new Dimension(28, 28));
        backButton.setPreferredSize(new Dimension(28, 28));
        nextButton.setPreferredSize(new Dimension(28, 28));

        panel.add(saveButton);
        panel.add(loadButton);
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(backButton);
        panel.add(nextButton);
        panel.add(regexCheckBox);
        panel.add(regexLabel);

        menu.add(saveItem);
        menu.add(loadItem);
        menu.addSeparator();
        menu.add(exitItem);

        searchMenu.add(searchItem);
        searchMenu.add(backItem);
        searchMenu.add(nextItem);
        searchMenu.add(useRegexItem);

        menuBar.add(menu);
        menuBar.add(searchMenu);
        setJMenuBar(menuBar);


        textArea.setLineWrap(true);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(new EmptyBorder(new Insets(0, 10, 10, 10)));
        add(BorderLayout.CENTER, scrollPane);
        add(jfc);
    }

    public void saveFile(JFileChooser jfc, JTextArea textArea) {
        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            try (PrintWriter printWriter = new PrintWriter(selectedFile)) {
                printWriter.print(textArea.getText());
            } catch (Exception e) {
                System.out.println("An exception occurred: " + e.getMessage());
            }
        }
    }

    public void loadFile(JFileChooser jfc, JTextArea textArea) {
        int returnValue = jfc.showOpenDialog(null);

        try {
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                final String text = new String(Files.readAllBytes(Paths.get(selectedFile.getPath())));
                textArea.setText(text);
            }
        } catch (IOException e) {
            System.out.println("Can't load file");
            textArea.setText("");
        }
    }

    public SwingWorker<Boolean, int[]> searchText(JTextArea textArea, ListIterator<String> iterator) {
        return new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                int[] indices = parseIndices(iterator, true);
                if (indices[1] == 0)
                    return false;
                else
                    publish(indices);
                return true;
            }

            @Override
            protected void process(List<int[]> chunks) {
                try {
                    boolean status = get();
                    if (status) {
                        int[] indices = chunks.get(chunks.size() - 1);
                        textArea.setCaretPosition(indices[1]);
                        textArea.select(indices[0], indices[1]);
                        textArea.grabFocus();
                    }
                } catch (Exception e) {
                    System.out.println("An error occurred.");
                }

            }
        };
    }

    public SwingWorker<Boolean, int[]> changeMatch(JTextArea textArea, ListIterator<String> iterator, boolean next) {
        return new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                int[] indices = parseIndices(iterator, next);
                if (indices[1] == 0)
                    return false;
                else
                    publish(indices);
                return true;
            }

            @Override
            protected void process(List<int[]> chunks) {
                try {
                    boolean status = get();
                    if (status) {
                        int[] indices = chunks.get(chunks.size() - 1);
                        textArea.setCaretPosition(indices[1]);
                        textArea.select(indices[0], indices[1]);
                        textArea.grabFocus();
                    }
                } catch (Exception e) {
                    System.out.println("An error occurred.");
                }
            }
        };
    }

    public ListIterator<String> findMatches(JTextArea textArea, JTextField searchField, boolean useRegex) {
        String pattern = searchField.getText();
        String text = textArea.getText();
        List<String> matchesFound = new ArrayList<>();
        Pattern p;
        if (useRegex) {
            p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } else {
            p = Pattern.compile(pattern, Pattern.LITERAL);
        }
        Matcher m = p.matcher(text);
        while (m.find()) {
            matchesFound.add(m.start() + "-" + m.end());
        }
        return matchesFound.listIterator();
    }

    public int[] parseIndices(ListIterator<String> iterator, boolean next) {
        String val = "";
        if (!iterator.hasPrevious() && !iterator.hasNext())
            return new int[]{0, 0};
        if (next) {
            val = iterNext(iterator, val);
            /*if (previousValue.equals(val)) {
                val = iterNext(iterator, val);
            }*/
        } else {
            val = iterPrevious(iterator, val);
            if (previousValue.equals(val)) {
                val = iterPrevious(iterator, val);
            }
        }
        int dashPos = val.indexOf('-');
        previousValue = val;
        System.out.println(previousValue);
        return new int[]{Integer.parseInt(val.substring(0, dashPos)), Integer.parseInt(val.substring(dashPos + 1))};
    }

    private String iterPrevious(ListIterator<String> iterator, String val) {
        if (iterator.hasPrevious()) {
            val = iterator.previous();
        }
        else {
            while(iterator.hasNext()) {
                val = iterator.next();
            }
            iterator.previous();
        }
        return val;
    }

    public String iterNext(ListIterator<String> iterator, String val) {
        if (iterator.hasNext()) {
            val = iterator.next();
        }
        else {
            while (iterator.hasPrevious()) {
                val = iterator.previous();
            }
            iterator.next();
        }
        return val;
    }

}

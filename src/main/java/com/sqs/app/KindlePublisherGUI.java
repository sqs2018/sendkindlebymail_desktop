package com.sqs.app;

import com.sqs.util.DateUtil;
import com.sqs.util.EmailUtil163;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import javax.imageio.ImageIO;

public class KindlePublisherGUI extends JFrame {
    private JTextField senderEmailField;
    private JPasswordField authCodeField;
    private JTextField kindleEmailField;
    private JTextField homeworkTitleField;
    private JTextArea homeworkArea;
    private JTextField filePathField;
    private JButton selectFileButton;
    private JButton screenshotButton;
    private JButton sendButton;

    // 配置文件路径
    private static final String CONFIG_FILE = "kindle_publisher_config.properties";
    // 截图保存目录
    private static final String SCREENSHOT_DIR = "screenshots";

    public KindlePublisherGUI() {
        setTitle("Kindle 电子书推送工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 620);
        setLocationRelativeTo(null);

        // 创建截图保存目录
        createScreenshotDir();

        initUI();
        setupLayout();
        attachListeners();

        // 启动时加载配置文件
        loadConfig();

        // 如果加载后标题为空，则设置为当前日期
        if (homeworkTitleField.getText().trim().isEmpty()) {
            setDefaultTitle();
        }
    }

    private void initUI() {
        senderEmailField = new JTextField(25);
        authCodeField = new JPasswordField(25);
        kindleEmailField = new JTextField(25);
        homeworkTitleField = new JTextField(25);
        homeworkArea = new JTextArea(6, 25);
        homeworkArea.setLineWrap(true);
        homeworkArea.setWrapStyleWord(true);
        filePathField = new JTextField(25);
        filePathField.setEditable(false);

        selectFileButton = new JButton("选择文件");
        screenshotButton = new JButton("截图");
        sendButton = new JButton("发送到Kindle");
    }

    /**
     * 创建截图保存目录
     */
    private void createScreenshotDir() {
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 设置默认标题：当前年月日，格式：yyyy-MM-dd
     */
    private void setDefaultTitle() {
        homeworkTitleField.setText(DateUtil.getCurrentDateTime() + "作业");
        saveConfig();
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        Font starFont = new Font("SansSerif", Font.BOLD, 14);
        Color starColor = new Color(255, 80, 80);

        // 行0：发件邮箱 (必填)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel senderLabel = new JLabel("发件邮箱:");
        JLabel star1 = new JLabel("*");
        star1.setFont(starFont);
        star1.setForeground(starColor);
        JPanel senderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        senderPanel.add(senderLabel);
        senderPanel.add(star1);
        formPanel.add(senderPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(senderEmailField, gbc);

        // 行1：邮箱授权码 (必填)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel authLabel = new JLabel("授权码:");
        JLabel star2 = new JLabel("*");
        star2.setFont(starFont);
        star2.setForeground(starColor);
        JPanel authPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        authPanel.add(authLabel);
        authPanel.add(star2);
        formPanel.add(authPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(authCodeField, gbc);

        // 行2：Kindle邮箱 (必填)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel kindleLabel = new JLabel("Kindle邮箱:");
        JLabel star3 = new JLabel("*");
        star3.setFont(starFont);
        star3.setForeground(starColor);
        JPanel kindlePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        kindlePanel.add(kindleLabel);
        kindlePanel.add(star3);
        formPanel.add(kindlePanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(kindleEmailField, gbc);

        // 行3：作业标题
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("作业标题:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(homeworkTitleField, gbc);

        // 行4：作业内容
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(new JLabel("作业内容:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        JScrollPane scrollPane = new JScrollPane(homeworkArea);
        scrollPane.setPreferredSize(new Dimension(350, 120));
        formPanel.add(scrollPane, gbc);

        // 行5：文件选择区域（文件路径+选择文件按钮+截图按钮）
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("附件文件:"), gbc);

        // 创建文件路径和按钮面板
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.add(filePathField, BorderLayout.CENTER);

        // 创建右侧按钮面板（包含选择文件和截图按钮）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(selectFileButton);
        buttonPanel.add(screenshotButton);
        filePanel.add(buttonPanel, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(filePanel, gbc);

        // 底部提示信息
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel hintLabel = new JLabel("* 为必填项。只能选择发送文本或者发送附件");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hintPanel.add(hintLabel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(hintPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部：发送按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(sendButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void attachListeners() {
        DocumentListener autoSaveListener = new DocumentListener() {
            private void save() {
                saveConfig();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                save();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                save();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                save();
            }
        };

        senderEmailField.getDocument().addDocumentListener(autoSaveListener);
        authCodeField.getDocument().addDocumentListener(autoSaveListener);
        kindleEmailField.getDocument().addDocumentListener(autoSaveListener);
        homeworkTitleField.getDocument().addDocumentListener(autoSaveListener);
        homeworkArea.getDocument().addDocumentListener(autoSaveListener);

        // 选择文件按钮事件
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(KindlePublisherGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    filePathField.setText(path);
                    saveConfig();
                }
            }
        });

        // 截图按钮事件
        screenshotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                takeScreenshot();
            }
        });

        // 发送按钮事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sender = senderEmailField.getText().trim();
                String authCode = new String(authCodeField.getPassword()).trim();
                String kindle = kindleEmailField.getText().trim();
                String title = homeworkTitleField.getText().trim();
                String homework = homeworkArea.getText().trim();


                if (sender.isEmpty() || authCode.isEmpty() || kindle.isEmpty()) {
                    JOptionPane.showMessageDialog(KindlePublisherGUI.this,
                            "发件邮箱、授权码和Kindle邮箱不能为空！",
                            "信息不完整", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (title.isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(KindlePublisherGUI.this,
                            "作业标题为空，确定要继续吗？",
                            "标题为空", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

               /* StringBuilder summary = new StringBuilder();
                summary.append("发件邮箱: ").append(sender).append("\n");
                summary.append("Kindle邮箱: ").append(kindle).append("\n");
                summary.append("作业标题: ").append(title.isEmpty() ? "无" : title).append("\n");
                summary.append("作业内容长度: ").append(homework.length()).append(" 字符\n");
                summary.append("附件文件: ").append(filePath.isEmpty() ? "无" : filePath).append("\n\n");
                summary.append("（演示版：实际发送需集成JavaMail）");

                JOptionPane.showMessageDialog(KindlePublisherGUI.this,
                        summary.toString(),
                        "准备发送", JOptionPane.INFORMATION_MESSAGE);*/


                boolean result = false;
                if(!homework.isEmpty()){
                    //将文本内容转成txt再发送
                    saveTxt(homework);
                }
                String filePath = filePathField.getText().trim();
                if (new File(filePath).exists()) {
                    result = new EmailUtil163(sender, authCode).sendTxtAttachment(kindle, title.isEmpty() ? "无" : title, homework.isEmpty() ? "无作业，看附件" : homework
                            , new File(filePath));
                }
                //发送成功
                if (result) {
                    JOptionPane.showMessageDialog(KindlePublisherGUI.this,
                            "作业发送成功！",
                            "发送成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(KindlePublisherGUI.this,
                            "作业发送失败，请检查配置和网络！",
                            "发送失败", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }

    /**
     * 执行截图功能
     */
    private void takeScreenshot() {
        try {
            // 隐藏当前窗口
            this.setVisible(false);
            // 稍微延迟一下，确保窗口完全隐藏
            Thread.sleep(500);

            // 创建机器人对象用于截屏
            Robot robot = new Robot();
            // 获取屏幕尺寸
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);

            // 截取全屏
            BufferedImage fullScreenImage = robot.createScreenCapture(screenRect);

            // 创建截图选择窗口
            JFrame selectionFrame = new JFrame();
            selectionFrame.setUndecorated(true);
            selectionFrame.setAlwaysOnTop(true);
            selectionFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            // 创建截图选择面板（带确定取消按钮）
            ScreenshotPanel screenshotPanel = new ScreenshotPanel(fullScreenImage, selectionFrame, this);
            selectionFrame.add(screenshotPanel);
            selectionFrame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "截图失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            // 显示窗口
            this.setVisible(true);
        }
    }

    /**
     * 保存截图的图片文件
     */
    public void saveScreenshot(BufferedImage image) {
        try {
            // 生成文件名：screenshot_年月日_时分秒.png
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            String filename = "screenshot_" + timestamp + ".png";

            // 完整的文件路径
            File screenshotFile = new File(SCREENSHOT_DIR, filename);

            // 保存图片
            ImageIO.write(image, "png", screenshotFile);

            // 将文件路径设置到文件选择框中
            filePathField.setText(screenshotFile.getAbsolutePath());


        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }


    public void saveTxt(String txtContent) {
        try {
            // 生成文件名：screenshot_年月日_时分秒.png
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            String filename = timestamp + ".txt";

            // 完整的文件路径
            File file = new File(SCREENSHOT_DIR, filename);
            FileWriter writer = new FileWriter(file);
            writer.write(txtContent);
            writer.close();


            // 将文件路径设置到文件选择框中
            filePathField.setText(file.getAbsolutePath());


        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    /**
     * 截图选择面板 - 内部类（带确定和取消按钮）
     */
    private class ScreenshotPanel extends JPanel {
        private BufferedImage fullImage;
        private Point startPoint;
        private Point endPoint;
        private boolean drawing = false;
        private Rectangle selectedRect;
        private JFrame parentFrame;
        private KindlePublisherGUI mainGUI;
        private JButton confirmButton;
        private JButton cancelButton;

        public ScreenshotPanel(BufferedImage image, JFrame frame, KindlePublisherGUI gui) {
            this.fullImage = image;
            this.parentFrame = frame;
            this.mainGUI = gui;
            setLayout(null); // 使用绝对布局放置按钮
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            // 创建确定按钮
            confirmButton = new JButton("确定");
            confirmButton.setVisible(false);
            confirmButton.setBackground(new Color(0, 120, 215));
            confirmButton.setForeground(Color.WHITE);
            confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 14));

            // 创建取消按钮
            cancelButton = new JButton("取消");
            cancelButton.setVisible(false);
            cancelButton.setBackground(new Color(255, 80, 80));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));

            // 添加按钮事件
            confirmButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (selectedRect != null) {
                        // 截取选区图像（考虑缩放比例）
                        int x = selectedRect.x * fullImage.getWidth() / getWidth();
                        int y = selectedRect.y * fullImage.getHeight() / getHeight();
                        int width = selectedRect.width * fullImage.getWidth() / getWidth();
                        int height = selectedRect.height * fullImage.getHeight() / getHeight();

                        // 确保坐标在有效范围内
                        x = Math.max(0, Math.min(x, fullImage.getWidth() - 1));
                        y = Math.max(0, Math.min(y, fullImage.getHeight() - 1));
                        width = Math.min(width, fullImage.getWidth() - x);
                        height = Math.min(height, fullImage.getHeight() - y);

                        if (width > 0 && height > 0) {
                            BufferedImage selectedArea = fullImage.getSubimage(x, y, width, height);
                            mainGUI.saveScreenshot(selectedArea);
                        }
                    }
                    // 关闭选择窗口并显示主窗口
                    parentFrame.dispose();
                    mainGUI.setVisible(true);
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 直接关闭选择窗口并显示主窗口
                    parentFrame.dispose();
                    mainGUI.setVisible(true);
                }
            });

            // 添加按钮到面板
            add(confirmButton);
            add(cancelButton);

            MouseHandler handler = new MouseHandler();
            addMouseListener(handler);
            addMouseMotionListener(handler);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // 绘制全屏截图（半透明效果）
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.drawImage(fullImage, 0, 0, getWidth(), getHeight(), this);

            // 如果正在绘制选区或已有选区，绘制选区矩形
            if ((drawing || selectedRect != null) && startPoint != null && endPoint != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(0, 120, 215, 50));

                int x = Math.min(startPoint.x, endPoint.x);
                int y = Math.min(startPoint.y, endPoint.y);
                int width = Math.abs(endPoint.x - startPoint.x);
                int height = Math.abs(endPoint.y - startPoint.y);

                // 填充选区
                g2d.fillRect(x, y, width, height);

                // 绘制选区边框
                g2d.setColor(new Color(0, 120, 215));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(x, y, width, height);

                // 显示选区尺寸
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(width + " x " + height, x + 5, y + 20);
            }
        }

        private class MouseHandler extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                endPoint = startPoint;
                selectedRect = null;
                drawing = true;
                confirmButton.setVisible(false);
                cancelButton.setVisible(false);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (startPoint != null && endPoint != null) {
                    // 计算选区
                    int x = Math.min(startPoint.x, endPoint.x);
                    int y = Math.min(startPoint.y, endPoint.y);
                    int width = Math.abs(endPoint.x - startPoint.x);
                    int height = Math.abs(endPoint.y - startPoint.y);

                    // 确保选区至少有一定大小
                    if (width > 20 && height > 20) {
                        selectedRect = new Rectangle(x, y, width, height);
                        drawing = false;

                        // 在选区右下角显示确定和取消按钮
                        confirmButton.setBounds(x + width - 180, y + height + 10, 80, 30);
                        cancelButton.setBounds(x + width - 90, y + height + 10, 80, 30);
                        confirmButton.setVisible(true);
                        cancelButton.setVisible(true);

                        repaint();
                    } else {
                        // 选区太小，重置
                        startPoint = null;
                        endPoint = null;
                        drawing = false;
                        repaint();
                    }
                }
            }
        }
    }

    /**
     * 保存配置到文件
     */
    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("sender.email", senderEmailField.getText().trim());
        props.setProperty("auth.code", new String(authCodeField.getPassword()).trim());
        props.setProperty("kindle.email", kindleEmailField.getText().trim());
        props.setProperty("homework.title", homeworkTitleField.getText().trim());

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Kindle Publisher Configuration");
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 从文件加载配置
     */
    private void loadConfig() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return;
        }

        try (FileInputStream in = new FileInputStream(configFile)) {
            props.load(in);

            String sender = props.getProperty("sender.email", "");
            String auth = props.getProperty("auth.code", "");
            String kindle = props.getProperty("kindle.email", "");
            String title = props.getProperty("homework.title", "");
            String homework = props.getProperty("homework", "");
            String filePath = props.getProperty("file.path", "");

            senderEmailField.setText(sender);
            authCodeField.setText(auth);
            kindleEmailField.setText(kindle);
            homeworkTitleField.setText(title);
            homeworkArea.setText(homework);
            filePathField.setText(filePath);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "加载配置文件失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new KindlePublisherGUI().setVisible(true);
            }
        });
    }
}
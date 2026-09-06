package com.hotelmanagement.view.phong;

import com.hotelmanagement.model.dto.DichVuDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * Lớp ServiceCard là thành phần giao diện (UI) dùng để hiển thị
 * thông tin của một dịch vụ dưới dạng thẻ (card).
 * 
 * Mỗi thẻ hiển thị:
 * - Tên dịch vụ
 * - Giá dịch vụ
 * - Hình ảnh (nếu có)
 * 
 * Thiết kế:
 * - Card có đổ bóng mờ, bo góc, trông hiện đại
 * - Có hiệu ứng hover (nảy lên khi di chuột vào)
 * - Click vào thẻ sẽ gọi callback onSelected để xử lý nghiệp vụ
 */
public class ServiceCard extends JPanel {
    private final DichVuDTO dichVu;
    private final java.util.function.Consumer<DichVuDTO> onSelected;

    /**
     * Constructor của ServiceCard
     * 
     * @param dichVu     Đối tượng DichVuDTO chứa thông tin dịch vụ
     * @param onSelected Callback function được gọi khi click vào thẻ
     */
    public ServiceCard(DichVuDTO dichVu, java.util.function.Consumer<DichVuDTO> onSelected) {
        this.dichVu = dichVu;
        this.onSelected = onSelected;

        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(160, 200));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(10, 10, 10, 10)));

        // Image Label
        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(JLabel.CENTER);
        lblImage.setPreferredSize(new Dimension(140, 120));

        // Load image based on maDV
        ImageIcon icon = loadServiceIcon(this.dichVu.getMaDV());
        if (icon != null) {
            lblImage.setIcon(icon);
        } else {
            lblImage.setText("No Image");
            lblImage.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblImage.setForeground(Color.LIGHT_GRAY);
        }
        add(lblImage, BorderLayout.CENTER);

        // Info Panel
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlInfo.setOpaque(false);

        JLabel lblName = new JLabel(this.dichVu.getTenDichVu());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setHorizontalAlignment(JLabel.CENTER);

        JLabel lblPrice = new JLabel(String.format("%,.0f VNĐ", this.dichVu.getGiaDV()));
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPrice.setForeground(new Color(46, 204, 113));
        lblPrice.setHorizontalAlignment(JLabel.CENTER);

        pnlInfo.add(lblName);
        pnlInfo.add(lblPrice);
        add(pnlInfo, BorderLayout.SOUTH);

        // Hover Effect & Click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(245, 247, 250));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                        new EmptyBorder(9, 9, 9, 9)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                        new EmptyBorder(10, 10, 10, 10)));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (ServiceCard.this.onSelected != null) {
                    ServiceCard.this.onSelected.accept(ServiceCard.this.dichVu);
                }
            }
        });
    }

    private ImageIcon loadServiceIcon(String maDV) {
        String[] extensions = { ".png", ".jpg", ".jpeg" };
        for (String ext : extensions) {
            String path = "/images/services/" + maDV + ext;
            URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                ImageIcon original = new ImageIcon(imgUrl);
                Image scaled = original.getImage().getScaledInstance(130, 110, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }
        return null; // or a default icon
    }
}

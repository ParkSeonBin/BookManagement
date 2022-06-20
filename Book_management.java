import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Book_management extends JFrame  {
	private JLabel bn = new JLabel("책 이름");
	private JTextField tf = new JTextField(10);
	private JButton SearchButton = new JButton("검색");
	private String sql ="";
	
	// DB 연동
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	int res ;
	// Dialog
	private MyDialog1 dialog1;
	private MyDialog2 dialog2;
	private MyDialog3 dialog3;
	// JTable
	String header[] = {"책 이름", "출판 연도", "지점", "권 수"};
	DefaultTableModel model;
	Object ob[][] = new Object[0][4];
	

	public Book_management() throws SQLException {
		super("책 재고 관리 프로그램");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container c = getContentPane();
		c.setLayout(new BorderLayout(10,10));
		
		// JMenu 생성
		JMenuBar mb = new JMenuBar();
		JMenu screenMenu = new JMenu("관리자");
		screenMenu.setToolTipText("관리자 모드입니다."); //툴팁 생성
		
		// Screen 메뉴에 메뉴아이템 생성 삽입
		JMenuItem [] menuItem = new JMenuItem[4];
		String[] itemTitle = {"추가", "수정", "삭제", "Exit"};
		
		dialog1 = new MyDialog1(this, "책 추가");
		dialog2 = new MyDialog2(this, "책 수정");
		dialog3 = new MyDialog3(this, "책 삭제");
		
		model = new DefaultTableModel(ob, header);
		JTable table = new JTable(model);
		JScrollPane scrollpane = new JScrollPane(table);
		
		MenuActionListener listener = new MenuActionListener(table);
		for(int i=0; i<menuItem.length; i++) {
			menuItem[i] = new JMenuItem(itemTitle[i]);
			menuItem[i].addActionListener(listener);
			screenMenu.add(menuItem[i]);
		}
		// 메뉴바에 메뉴 삽입
		mb.add(screenMenu); // Screen 메뉴 삽입
			
		setJMenuBar(mb);
		
		connect();
		
		JPanel topPanel = new JPanel();
		topPanel.add(bn);
		topPanel.add(tf);
		topPanel.add(SearchButton);
		SearchButton.addActionListener(new Listener(this));
		c.add("North",topPanel);
		
		c.add("Center",scrollpane);
		table_print(); 
		
		setSize(400,400);
		setVisible(true);
	}
	class Listener implements ActionListener{
		JFrame frame;
		public Listener(JFrame f){
			frame =f;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String search = tf.getText();
			String year = null;;
	        String wheres;
	        int books;
			try {
				String sql = "select *from javadb where name = ?";
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, search);
				
				ResultSet rs = null;
		        rs = pstmt.executeQuery();
		        while(rs.next()) {
		            year = rs.getString("year");
		            wheres = rs.getString("wheres");
		            books = rs.getInt("books");
		            JOptionPane.showMessageDialog(frame, "책 이름 : " + search + ", 출판 연도 : " + year + ", 지점 : " + wheres + ", 권 수 : " + books,"검색 결과",JOptionPane.INFORMATION_MESSAGE);
		        }
		        if(year == null)
		        	JOptionPane.showMessageDialog(frame, "검색 값이 없습니다.", "error",JOptionPane.ERROR_MESSAGE);
		        
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			tf.setText("");
		}
	}
	class MenuActionListener implements ActionListener { 
		JTable table;
		public MenuActionListener(JTable t){
			table = t;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			switch(cmd) {
				case "추가" : 
					dialog1.setVisible(true);
					String name1 = dialog1.getname();
					String year1 = dialog1.getyear();
					String wheres1 = dialog1.getwheres();
					String books1 = dialog1.getbooks();
					int book1 = Integer.parseInt(books1);
					sql = "insert into javadb values(?,?,?,?)";
					try {
						pstmt = con.prepareStatement(sql);
						pstmt.setString(1, name1);
				        pstmt.setString(2, year1);
				        pstmt.setString(3, wheres1);
				        pstmt.setInt(4, book1);
				        res = pstmt.executeUpdate();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				    if(res > 0){
				    	System.out.println("처리 완료");
				    	Object [] insertrow = {name1, year1, wheres1,book1};
				    	model.addRow(insertrow);
				    }
					break;
				case "수정" :	
					dialog2.setVisible(true);
					String bname2 = dialog2.getbname();
					String name2 = dialog2.getname();
					String year2 = dialog2.getyear();
					String wheres2 = dialog2.getwheres();
					String books2 = dialog2.getbooks();
					int book2 = Integer.parseInt(books2);
					sql = "update javadb set name = ?, year = ?, wheres = ?, books = ? where name = ?";
					try {
						pstmt = con.prepareStatement(sql);
						pstmt.setString(1, name2);
				        pstmt.setString(2, year2);
				        pstmt.setString(3, wheres2);
				        pstmt.setInt(4, book2);
				        pstmt.setString(5, bname2);
					        
				        res = pstmt.executeUpdate();
					} catch (SQLException e1) {
						e1.printStackTrace();		
					}
			        if(res > 0){
			            System.out.println("업데이트 완료");
			            table_print(); 
			        } else {
			            System.out.println("값이 없습니다.");
					}
					break;
				case "삭제" : 
					dialog3.setVisible(true);
					String name3 = dialog3.getname();
					sql = "delete from javadb where name =?";
					try {
						pstmt = con.prepareStatement(sql);
						pstmt.setString(1, name3);
						res = pstmt.executeUpdate();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
			        if(res > 0){
			            System.out.println("삭제되었습니다.");
						table_print(); 
			        } else {
			            System.out.println("값이 없습니다.");
					}
					break;
				case "Exit" : 
					System.exit(0); 
					if(pstmt != null)
						try {
							if(pstmt != null)
								pstmt.close();
							if(con != null) 
								con.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					break;
			}
		}
	}
	private void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sampledb", "root","1234"); 
			System.out.println("DB 연결 완료");
		} catch (Exception e) {
			System.out.println("DB 접속 오류");
		}
	}
	public void table_print() {
		model.setNumRows(0);
		try {
			String sql = "Select *from javadb";
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				String year = rs.getString("year");
				String wheres = rs.getString("wheres");
				String books = rs.getString("books");
				
				Object data[] = {name, year, wheres, books};
				model.addRow(data);
			}
		} catch (Exception e) {
			System.out.println("select() 실행 오류");
		}
	}
	
	
	public static void main(String [] args) throws ClassNotFoundException, SQLException {
		new Book_management();
	}
}

class MyDialog1 extends JDialog {
	private JTextField tf1, tf2, tf3, tf4;
	private JLabel l1, l2, l3, l4;
	private JButton okButton = new JButton("OK");
	
	public MyDialog1(JFrame frame, String title) {
		super(frame, title,true);
		setLayout(new GridLayout(5,2));
		
		l1 = new JLabel("책 이름");
		tf1 = new JTextField(20);
		add(l1);
		add(tf1);
		
		l2 = new JLabel("출판 연도");
		tf2 = new JTextField(20);
		add(l2);
		add(tf2);
		
		l3 = new JLabel("지점");
		tf3 = new JTextField(20);
		add(l3);
		add(tf3);
		
		l4 = new JLabel("권 수");
		tf4 = new JTextField(20);
		add(l4);
		add(tf4);
		
		add(okButton);
		tf1.setText("");
		tf2.setText("");
		tf3.setText("");
		tf4.setText("");
		setSize(400,200);
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
	}
	public String getname() {
		String name = tf1.getText();
		if(name.length() == 0) return null;
		else { 
			tf1.setText("");
			return name;
		}
	}
	public String getyear() {
		String year = tf2.getText();
		if(year.length() == 0) return null;
		else { 
			tf2.setText("");
			return year;
		}
	}
	public String getwheres() {
		String wheres = tf3.getText();
		if(wheres.length() == 0) return null;
		else { 
			tf3.setText("");
			return wheres;
		}
	}
	public String getbooks() {
		String books = tf4.getText();
		if(books.length() == 0) return null;
		else { 
			tf4.setText("");
			return books;
		}
	}
}

class MyDialog2 extends JDialog {
	private JTextField tf0, tf1, tf2, tf3, tf4;
	private JLabel l0, l1, l2, l3, l4;
	private JButton okButton = new JButton("OK");
	
	public MyDialog2(JFrame frame, String title) {
		super(frame, title,true);
		setLayout(new GridLayout(6,2));
		
		l0 = new JLabel("변경 전 책 이름");
		tf0 = new JTextField(20);
		add(l0);
		add(tf0);
		
		l1 = new JLabel("변경 후 책 이름");
		tf1 = new JTextField(20);
		add(l1);
		add(tf1);
		
		l2 = new JLabel("변경 후 출판 연도");
		tf2 = new JTextField(20);
		add(l2);
		add(tf2);
		
		l3 = new JLabel("변경 후 지점");
		tf3 = new JTextField(20);
		add(l3);
		add(tf3);
		
		l4 = new JLabel("변경 후 권 수");
		tf4 = new JTextField(20);
		add(l4);
		add(tf4);
		
		add(okButton);
		
		setSize(400,300);
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}
	public String getbname() {
		String bname = tf0.getText();
		if(bname.length() == 0) return null;
		else { 
			tf0.setText("");
			return bname;
		}
	}
	public String getname() {
		String name = tf1.getText();
		if(name.length() == 0) return null;
		else { 
			tf1.setText("");
			return name;
		}
	}
	public String getyear() {
		String year = tf2.getText();
		if(year.length() == 0) return null;
		else { 
			tf2.setText("");
			return year;
		}
	}
	public String getwheres() {
		String wheres = tf3.getText();
		if(wheres.length() == 0) return null;
		else { 
			tf3.setText("");
			return wheres;
		}
	}
	public String getbooks() {
		String books = tf4.getText();
		if(books.length() == 0) return null;
		else { 
			tf4.setText("");
			return books;
		}
	}
}

class MyDialog3 extends JDialog {
	private JTextField tf1 ;
	private JLabel l1;
	private JButton okButton = new JButton("OK");
	
	public MyDialog3(JFrame frame, String title) {
		super(frame, title,true);
		setLayout(new GridLayout(2,2));
		
		l1 = new JLabel("삭제할 책 이름");
		tf1 = new JTextField(20);
	
		add(l1);
		add(tf1);
		add(okButton);
		setSize(300,100);
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}
	public String getname() {
		String name = tf1.getText();
		if(name.length() == 0) return null;
		else { 
			tf1.setText("");
			return name;
		}
	}
}

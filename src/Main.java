import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class Main {
    static String url = "jdbc:mysql://localhost:3306/algorithm_termproject?useUnicode=true&characterEncoding=utf8";
    static String userName = "root";
    static String password = "12345";
    static int userNum;
    static user[] users;
    static Station_info[] stations;
    public static void main(String[] args) throws Exception {
        // mysql 서버와 연결. 이 connection을 통해 쿼리 보내고 결과 받음
        //Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url, userName, password);
        //Statement statement = connection.createStatement();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please Enter the number of user : ");
        userNum = Integer.parseInt(br.readLine());
        users = new user[userNum];
        // select * from Station_info 의 정보 담아주기
        //Init_setter(resultSet);
        PreparedStatement TogetSize = connection.prepareStatement("SELECT count(*) AS size FROM station_info");
        ResultSet Size =  TogetSize.executeQuery();
        int size = Size.getInt(1);
        // 이제 역의 전체 수를 얻어왔으니 전체 역 정보를 담을 배열을 초기화 하겠습니다.
        stations = new Station_info[size];
        // 이제 전체 역의 정보를 위 배열에 차례차례 넣겠습니다.
        PreparedStatement statement = connection.prepareStatement("SELECT * AS size FROM station_info");
        ResultSet resultSet = statement.executeQuery();
        Init_setter(resultSet);

        for(int i=0; i<userNum; i++) {
            System.out.println("Please Enter the location of user " + i);
            String tempName = br.readLine();
            //PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_info WHERE Station_name = ?");
            //PreparedStatement statement = connection.prepareStatement("SELECT * AS size FROM station_info");
            //statement.setString(1, tempName);
            //ResultSet resultSet = statement.executeQuery();

        }
    }

    static void Init_setter(ResultSet resultSet) throws SQLException {
        String tempLine, name;
        float lat, lon;
        int cafe, store, sum_around;
        boolean dptstore, cinema, is_final;
        int iter = 0;
        while(resultSet.next()) {
            tempLine = resultSet.getString(1);
            name = resultSet.getString(2);
            lat = resultSet.getFloat(3);
            lon = resultSet.getFloat(4);
            cafe = resultSet.getInt(5);
            store = resultSet.getInt(6);
            sum_around = resultSet.getInt(7);
            dptstore = resultSet.getBoolean(8);
            cinema = resultSet.getBoolean(9);
            is_final = resultSet.getBoolean(10);
            stations[iter] = new Station_info(tempLine, name, lat, lon, cafe,
                    store, sum_around, dptstore, cinema, is_final);
            iter++;
        }
    }

    // 역간 이동에 대한 클래스. 호선정보, 출발역, 도착역, 거리, 종착역 여부를 정보로 갖는다.
    // 역 정보 클래스. 역의 이름과 좌표값을 가진다.
    static class Station_info {
        String Line_Info;
        String Station_name;
        float lat, lon;
        int cafe, store, sum_around;
        boolean dptstore, cinema, Is_Final;
        public Station_info(String Line_Info, String Station_name, float x, float y, int cafe, int store,
                           int sum_around, boolean dptstore, boolean cinema, boolean Is_Final) {
            this.Line_Info = Line_Info;
            this.Station_name = Station_name;
            this.lat = x;
            this.lon = y;
            this.cafe = cafe;
            this.store = store;
            this.sum_around = sum_around;
            this.dptstore = dptstore;
            this.cinema = cinema;
            this.Is_Final = Is_Final;
        }
    }
    //환승체크
    // 출발지 검색
    //
}
// user 클래스. 처음 출발하는 역 노드, 누적 거리값, 여태 지나온 역들을 담을 queue를 가진다.
class user {
    Main.Station_info Current_station;
    Queue<Main.Station_info> Node_queue;
    float Cumulated_dist;
    public user(Main.Station_info Current_station, float Cumulated_dist) {
        this.Current_station = Current_station;
        this.Cumulated_dist = Cumulated_dist;
        Node_queue = new LinkedList<>();
    }
}


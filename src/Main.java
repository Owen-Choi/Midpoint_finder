import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    static String url = "jdbc:mysql://localhost:3306/algorithm_termproject?useUnicode=true&characterEncoding=utf8";
    static String userName = "root";
    static String password = "12345";
    static int userNum;
    static user[] users;
    static Station_info[] stations;
    static Station_distance[] station_distances;
    static Transfer_info[] transfer_infos;
    public static void main(String[] args) throws Exception {
        // mysql 서버와 연결. 이 connection을 통해 쿼리 보내고 결과 받음
        //Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url, userName, password);
        //Statement statement = connection.createStatement();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please Enter the number of user : ");
        userNum = Integer.parseInt(br.readLine());
        users = new user[userNum];
        Station_Info_setter(connection);
        Station_distance_setter(connection);
        Transfer_info_setter(connection);
        for(int i=0; i<userNum; i++) {
            System.out.println("Please Enter the location of user " + i);
            String tempName = br.readLine();
            //PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_info WHERE Station_name = ?");
            //PreparedStatement statement = connection.prepareStatement("SELECT * AS size FROM station_info");
            //statement.setString(1, tempName);
            //ResultSet resultSet = statement.executeQuery();
            Station_info init_station = null;
            do {
                init_station = checker(tempName);
            }while(init_station == null);
            users[i] = new user(init_station);
            System.out.println(users[i].Current_station.Station_name);
        }
    }
    static void Station_Info_setter(Connection connection) throws SQLException {
        PreparedStatement TogetSize = connection.prepareStatement("SELECT count(*) FROM station_info");
        ResultSet Size =  TogetSize.executeQuery();
        while(Size.next())
            stations = new Station_info[Size.getInt(1)];

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_info");
        ResultSet resultSet = statement.executeQuery();

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
            stations[iter++] = new Station_info(tempLine, name, lat, lon, cafe,
                    store, sum_around, dptstore, cinema, is_final);
        }
    }

    static void Station_distance_setter(Connection connection) throws SQLException{
        PreparedStatement TogetSize = connection.prepareStatement("SELECT count(*) FROM station_distance");
        ResultSet Size = TogetSize.executeQuery();
        while(Size.next())
            station_distances = new Station_distance[Size.getInt(1)];

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_distance");
        ResultSet resultSet = statement.executeQuery();

        String Line_info;
        String Dpt_Station, Dest_Station;
        float Distance;
        int iter = 0;
        while(resultSet.next()) {
            Line_info = resultSet.getString(1);
            Dpt_Station = resultSet.getString(2);
            Dest_Station = resultSet.getString(3);
            Distance = resultSet.getFloat(4);
            station_distances[iter++] = new Station_distance(Line_info, Dpt_Station, Dest_Station, Distance);
        }
    }

    static void Transfer_info_setter(Connection connection) throws SQLException{
        PreparedStatement togetSize = connection.prepareStatement("SELECT count(*) FROM transfer_info");
        ResultSet Size = togetSize.executeQuery();
        while(Size.next())
            transfer_infos = new Transfer_info[Size.getInt(1)];

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM transfer_info");
        ResultSet resultSet = statement.executeQuery();
        String Line_info, Station_name, Transfer_line;
        float Transfer_value;
        int iter = 0;
        while(resultSet.next()) {
            Line_info = resultSet.getString(1);
            Station_name = resultSet.getString(2);
            Transfer_line = resultSet.getString(3);
            Transfer_value = resultSet.getFloat(4);
            transfer_infos[iter++] = new Transfer_info(Line_info, Station_name, Transfer_line, Transfer_value);
        }
    }

    static Station_info checker(String tempName) {
        for(Station_info temp : stations) {
            if(temp.Station_name.equals(tempName)) {
                return temp;
            }
        }
        System.out.println(":: Invalid station name :: ");
        return null;
    }

    // 역간 이동에 대한 클래스. 호선정보, 출발역, 도착역, 거리, 종착역 여부를 정보로 갖는다.
    // 역 정보 클래스. 역의 이름과 좌표값을 가진다.
    //환승체크
    // 출발지 검색
    //
}

class Station_distance {
    String Line_info;
    String Dpt_Station, Dest_Station;
    float Distance;
    public Station_distance(String Line_info, String Dpt_Station, String Dest_Station, float Distance) {
        this.Line_info = Line_info;
        this.Dpt_Station = Dpt_Station;
        this.Dest_Station = Dest_Station;
        this.Distance = Distance;
    }
}

class Station_info {
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

class Transfer_info {
    String Line_info;
    String Station_name;
    String Transfer_line;
    float Transfer_value;
    public Transfer_info(String Line_info, String Station_name, String Transfer_line, float Transfer_value) {
        this.Line_info = Line_info;
        this.Station_name = Station_name;
        this.Transfer_line = Transfer_line;
        this.Transfer_value = Transfer_value;
    }
}

// user 클래스. 처음 출발하는 역 노드, 누적 거리값, 여태 지나온 역들을 담을 queue를 가진다.
class user {
    // 일단은 최초에 찾은 하나의 역만을 갖는다. 후에 문제가 생기면 리스트로 바꾸던가 해야겠다.
    Station_info Current_station;
    Queue<Queue<Station_info>> Ways;
    public user(Station_info Current_station) {
        this.Current_station = Current_station;
        Ways = new LinkedList<>();
    }
}


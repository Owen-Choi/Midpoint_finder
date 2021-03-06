import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    static String url = "jdbc:mysql://localhost:3306/algorithm_termproject?useUnicode=true&characterEncoding=utf8";
    static String userName = "root";
    static String password = "12345";
    static final int INF = 10000000;
    static int userNum;
    static user[] users;
    static Station_info[] stations;
    static Station_distance[] station_distances;
    static Transfer_info[] transfer_infos;
    static Sum[] distance_sum;
    static boolean Cafe;
    static boolean Store;
    static boolean Both;
    static boolean Dept_store;
    static boolean Cinema;
    public static void main(String[] args) throws Exception {
        // mysql 서버와 연결. 이 connection을 통해 쿼리 보내고 결과 받음
        Connection connection = DriverManager.getConnection(url, userName, password);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please Enter the number of user : ");
        userNum = Integer.parseInt(br.readLine());
        users = new user[userNum];
        Station_Info_setter(connection);
        Station_distance_setter(connection);
        Transfer_info_setter(connection);
        for(int i=0; i<userNum; i++) {
            System.out.println("Please Enter the location of user " + (i+1));
            String tempName = br.readLine();
            Station_info init_station = null;
            do {
                init_station = checker(tempName);
            }while(init_station == null);
            users[i] = new user(init_station);
            System.out.println(users[i].Current_station.Station_name);
        }
        Member[][] members = new Member[281][281];
        String [][] lines = new String[281][281];
        distance_sum = new Sum[281];
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_distance");
        ResultSet resultSet = statement.executeQuery();
        Station_info Pivot = null, Changes = null;

        for(int i=0; i<281; i++) {
            for(int k=0; k<281; k++) {
                members[i][k] = new Member();
                if(i == k)
                    members[i][k].dist = 0;
                else {
                    members[i][k].Line_info = "INF";
                    members[i][k].dist = INF;
                }
                lines[i][k] = "INF";
            }
        }
        Line_init(lines);
        for(int i=0; i<281; i++) {
            Pivot = stations[i];            //인덱스로 접근해서 역 정보 할당
            for(int k=0; k<281; k++) {
                //방향성이 없기 때문에 역으로도 저장이 되어야 한다.
                Changes = stations[k];
                // 쿼리문의 결과와 배열들을 매개변수로 넣어서, 플로이드 와샬 알고리즘으로 채워나갈 테이블에
                // 초기 값을 넣어줍니다.
                Find_dist(statement.executeQuery(), members, Pivot, Changes,i, k, lines);
            }
        }
        //floydalgorithm은 환승을 고려하지 않습니다.
        //floydalgorithm(members, lines);
        //floydalgorithm2는 환승을 고려합니다.
        floydalgorithm2(members, lines);
    }
    // 쿼리문을 건네받아서 배열을 채우는 함수입니다.
    // 역의 이름을 루프를 돌며 비교하고 일치하는 역을 찾으면 정보를 할당하고 함수를 종료합니다.
    static void Find_dist(ResultSet RS, Member[][] members, Station_info Pivot, Station_info Changes,
                          int i, int k, String[][] lines) throws SQLException{
        String From, To;
        float dist;
        // 전체 쿼리문에서 출발역, 도착역 이름으로 tuple 찾아서 환승값 추가
        while(RS.next()) {
            From = RS.getString(2);
            To = RS.getString(3);
            dist = RS.getFloat(4);
            if(Pivot.Station_name.equals(Changes.Station_name)) {
                members[i][k].dist = 0;
                members[k][i].dist = 0;
                break;
            }
            else if((From.equals(Pivot.Station_name) && To.equals(Changes.Station_name)) ||
                    ((From.equals(Changes.Station_name)) && (To.equals(Pivot.Station_name)))){
                members[i][k].Line_info = Pivot.Line_Info;
                members[i][k].dist = dist;
                members[k][i].Line_info = Pivot.Line_Info;
                members[k][i].dist = dist;
                break;
            }
        }

    }

    // 역 정보를 담는 배열을 최초에 데이터베이스에서 가져와서 저장해주는 함수입니다.
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
            // 모두 역에 저장을 해줍니다.
            stations[iter++] = new Station_info(tempLine, name, lat, lon, cafe,
                    store, sum_around, dptstore, cinema, is_final, iter-1);
        }
    }
    //역간 거리에 대한 정보를 최초에 데이터베이스에서 가져와서 저장해줍니다.
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
    // 환승에 대한 정보를 최초에 데이터베이스에서 가져와서 저장해줍니다.
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
    // 메인 알고리즘 파트입니다.
    static void floydalgorithm2 (Member[][] w, String[][] line_num) {
        int maxnum = 281;
        boolean flag = false;
        for(int k=0; k<maxnum; k++)
        {
            for(int i=0; i<maxnum; i++)
            {
                for(int j=0; j<maxnum; j++)
                {
                    if(w[i][j].dist > w[i][k].dist + w[k][j].dist && line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist;
                    else if(w[i][j].dist > w[i][k].dist + w[k][j].dist &&
                            !line_num[j][k].equals(line_num[i][j])) {
                        // 환승 지점은 k 입니다.
                        float tw=0;
                        // 환승 테이블에서 정보를 찾는 부분입니다.
                        flag = false;
                        for (int p=0;p<90; p++) {
                            if(transfer_infos[p].Station_name.equals(stations[k].Station_name)
                            && transfer_infos[p].Transfer_line.equals(stations[j].Line_Info) &&
                            transfer_infos[p].Line_info.equals(stations[i].Line_Info)) {
                                //해당하는 값을 환승 테이블에서 찾았다면 가져와서 더해줍니다.
                                tw = transfer_infos[p].Transfer_value;
                                w[i][j].dist = w[i][k].dist + w[k][j].dist + tw;
                                flag = true;
                                break;
                            }
                        }
                        if(!flag){
                            if(w[i][j].dist > w[i][k].dist + w[k][j].dist)
                                w[i][j].dist = w[i][k].dist + w[k][j].dist;
                        }
                    }
                }
            }
        }
        // 이제 2차원 배열이 준비되었습니다.
        calc_fair(w);
    }

    static void floydalgorithm(Member[][] w, String[][] line_num) {
        int maxnum = 281;
        int tw = 0;
        for(int k=0; k<maxnum; k++)
        {
            for(int i=0; i<maxnum; i++)
            {
                for(int j=0; j<maxnum; j++)
                {
                    if(w[i][j].dist > w[i][k].dist + w[k][j].dist && line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist;
                    else if(w[i][j].dist > w[i][k].dist + w[k][j].dist + tw && !line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist + tw;
                }
            }
        }
        calc_fair(w);
    }
    // 호선의 정보만을 담는 배열을 초기화하는 함수입니다.
    static void Line_init(String[][] line_num) {
        for(int x=0; x<281; x++) {
            for(int y=0; y<281; y++) {
                line_num[x][y] = stations[y].Line_Info;
            }
        }
    }
    // 채워진 테이블을 바탕으로 최적의 역을 찾아내는 함수입니다.
    static void calc_fair(Member[][] members) {
        int index = 0;
        float distSum;
        int[] start_indexes = new int[userNum];
        int dest_index = 0;
        for(int i=0; i<281; i++) {
            distSum = 0;
            for(int k=0; k<userNum; k++) {
                index = users[k].Current_station.station_num;
                // 각 유저의 출발지로부터 한 역까지 걸리는 모든 가중치를 더합니다.
                // k번째 유저의 출발지는 index입니다.
                start_indexes[k] = index;
                // k번째 유저의 도착지는 i
                dest_index = i;
                distSum += members[index][i].dist;
                distSum += around_operator(i);
                distSum = Math.round(distSum * 100 / 100.0);
            }
            // i역에 대해서 모든 유저들의 가중치 총 합을 저장.
            distance_sum[i] = new Sum(start_indexes, dest_index, distSum);
        }
        // 총합 값의 저장이 끝났으면 정렬 후 분산을 구한다.
        // 먼저 정렬을 수행한다.
        Arrays.sort(distance_sum, new Comparator<Sum>() {
            @Override
            public int compare(Sum o1, Sum o2) {
                if(o1.dist_sum < o2.dist_sum)
                    return -1;
                else
                    return 1;
            }
        });

        // 그리고 (일단은) 상위 n개의 값 중에서 분산을 비교한다.
        // (값 - 값들의 평균)^2의 평균
        float tempSum;
        float tempAVG;
        float min = INF;
        float resultX, resultY;
        resultX = resultY = 0;
        // 거리와 분산 모두 공평하게 고려하기 최적의 값은 80 근처로 추정됩니다.
       for(int i=0; i<80; i++) {
            tempSum = 0;
            tempAVG = 0;
            for(int k=0; k<userNum; k++) {
                tempSum += members[distance_sum[i].start_pos[k]][distance_sum[i].dest_pos].dist;
            }
            tempSum = Math.round(tempSum * 100 / 100.0);
            // 합을 구했으니 이를 바탕으로 평균을 구하고 나아가 분산을 구하겠습니다.
            tempAVG = tempSum / userNum;
            tempSum = 0;
            for(int j=0; j<userNum; j++) {
                tempSum += Math.pow
                        (Math.round((tempAVG - members[distance_sum[i].start_pos[j]][distance_sum[i].dest_pos].dist)
                        * 100) / 100.0,2);  //소수점 아래 둘째 자리까지만 고려합니다.

            }
            tempAVG = tempSum/userNum;
            // 분산을 구했습니다. 이 정보들을 최소값과 비교하고 저장해주겠습니다.
            tempAVG = Math.round(tempAVG * 100 / 100.0);
           // 분산이 0이라는 것은 무언가 잘못됐음을 의미합니다. 과감하게 제외하도록 하겠습니다.
           if(tempAVG == 0)
               continue;
            if(tempAVG < min) {
                min = tempAVG;
                resultX = stations[distance_sum[i].dest_pos].lat;
                resultY = stations[distance_sum[i].dest_pos].lon;
            }
        }
        System.out.println(get_station_by_pos(resultX, resultY).Station_name);
    }
    //체크된 옵션에 따라 가중치를 달리 할당해주는 메서드입니다.
    static float around_operator(int index) {
        float around_value = 0;
        if(Cafe)
            around_value -= stations[index].cafe * 0.1;
        if(Store)
            around_value -= stations[index].store * 0.1;
        if(Dept_store)
            around_value += stations[index].dptstore ? 0 : INF;
        if(Cinema)
            around_value += stations[index].cinema ? 0 : INF;
        return around_value;
    }
    // 매개변수로 넘긴 역의 이름이 존재하는 역인지 확인하는 함수입니다.
    static Station_info checker(String tempName) {
        for(Station_info temp : stations) {
            if(temp.Station_name.equals(tempName)) {
                return temp;
            }
        }

        System.out.println(":: Invalid station name :: ");
        return null;
    }

    // 최종 결과로 얻은 좌표값을 역 이름으로 바꿔서 반환하는 함수입니다.
    static Station_info get_station_by_pos(float x, float y) {
        for(Station_info temp : stations) {
            if(temp.lat == x && temp.lon == y)
                return temp;
        }
        return null;
    }
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
// 역 정보를 담을 class 입니다.
class Station_info {
    String Line_Info;
    String Station_name;
    float lat, lon;
    int cafe, store, sum_around;
    int station_num;
    boolean dptstore, cinema, Is_Final;
    public Station_info(String Line_Info, String Station_name, float x, float y, int cafe, int store,
                        int sum_around, boolean dptstore, boolean cinema, boolean Is_Final, int station_num) {
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
        this.station_num = station_num;
    }
}
// 환승 정보를 담을 class 입니다.
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
// 사용자의 정보를 담을 class 입니다.
class user {
    Station_info Current_station;
    public user(Station_info Current_station) {
        this.Current_station = Current_station;
    }
}

// Floyd-warshall 테이블의 tuple 하나하나를 이룰 데이터의 class입니다.
class Member {
    String Line_info;
    float dist;
    public Member() {
        Line_info = null;
        dist = 0.0f;
    }
}
// 거리의 합 값과 더불어 출발지와 도착지의 정보를 함께 가지고있어야 하기 때문에 별도의 class로 정의하였습니다.
class Sum {
    int[] start_pos;
    int dest_pos;
    // 정렬은 이 dist_sum을 기준으로 이루어집니다.
    float dist_sum;
    public Sum(int[] start_pos, int dest_pos, float dist_sum) {
        this.start_pos = start_pos;
        this.dist_sum = dist_sum;
        this.dest_pos = dest_pos;
    }
}



import {StyleSheet, Text, View, Modal, TouchableOpacity} from 'react-native';
// import { Picker } from '@react-native-community/picker';
import {Calendar, LocaleConfig} from 'react-native-calendars';
import {format, addDays} from 'date-fns';
import {useEffect, useState, useCallback} from 'react';
import DatePicker from 'react-native-modern-datepicker';
import Ionicons from 'react-native-vector-icons/Ionicons';

import {createStackNavigator} from '@react-navigation/stack';
import {NavigationContainer, useFocusEffect} from '@react-navigation/native';
import ScheduleScreen from './ScheduleScreen';
import SchedulePreview from '../components/schedule/SchedulePreview';
import AsyncStorage from '@react-native-async-storage/async-storage';
import axiosInstance from '../util/Interceptor';
import refreshState from '../../atoms/refreshState';
import {useRecoilState} from 'recoil';

// 달력 현지화
LocaleConfig.locales['fr'] = {
  monthNames: [
    '01',
    '02',
    '03',
    '04',
    '05',
    '06',
    '07',
    '08',
    '09',
    '10',
    '11',
    '12',
  ],
  monthNamesShort: [
    '01',
    '02',
    '03',
    '04',
    '05',
    '06',
    '07',
    '08',
    '09',
    '10',
    '11',
    '12',
  ],
  dayNames: [
    '일요일',
    '월요일',
    '화요일',
    '수요일',
    '목요일',
    '금요일',
    '토요일',
  ],
  dayNamesShort: ['일', '월', '화', '수', '목', '금', '토'],
  today: "Aujourd'hui",
};

LocaleConfig.defaultLocale = 'fr';

const customTheme = {
  'stylesheet.calendar.header': {
    // 일요일 색상 변경
    dayTextAtIndex0: {
      color: '#D93939',
    },
    // 월 ~ 토요일 검정색 변경
    dayTextAtIndex1: {
      color: 'black',
    },
    dayTextAtIndex2: {
      color: 'black',
    },
    dayTextAtIndex3: {
      color: 'black',
    },
    dayTextAtIndex4: {
      color: 'black',
    },
    dayTextAtIndex5: {
      color: 'black',
    },
    dayTextAtIndex6: {
      color: 'black',
    },
    monthText: {
      // 월 글씨 크기
      fontSize: 24,
      fontWeight: 'bold',
    },
    yearText: {
      // 연도 글씨 크기
      fontSize: 24,
      fontWeight: 'bold',
    },
  },
  'stylesheet.day.basic': {
    base: {
      width: 44,
      height: 44,
      alignItems: 'center',
      marginTop: 10,
    },
  },
  selectedDayBackgroundColor: 'black', // 선택된 날짜 배경색
  arrowColor: 'black',
  todayTextColor: 'white',
  todayBackgroundColor: '#D93939',
  // 일자 폰트 (1, 2, ... , 31)
  textDayFontFamily: 'Jost-Medium',
  textDayFontSize: 20,
  // 요일 폰트 (일, 월, ..., 토)
  textDayHeaderFontFamily: 'Pretendard-SemiBold',
  textDayHeaderFontSize: 18,
};

export default function CalendarScreen({route, navigation}) {
  // 보여줄 달력의 연월 정보
  const [calendarDate, setCalendarDate] = useState(
    format(new Date(), 'yyyy-MM-dd'),
  );
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth() + 1);
  // 연월 선택창 모달 설정
  const [isModalVisible, setisModalVisible] = useState(false);
  const showPickerModal = () => {
    setisModalVisible(true);
  };
  const hidePickerModal = () => {
    setisModalVisible(false);
  };
  // 연월 선택했을 경우 실행될 함수
  const handleDatePickerChange = (selectedYear, selectedMonth) => {
    setCalendarDate(`${selectedYear}-${selectedMonth}-01`);
    setCurrentYear(selectedYear);
    setCurrentMonth(selectedMonth);
    hidePickerModal();
  };

  const [schedules, setSchedules] = useState([]);
  const [refresh, setRefresh] = useRecoilState(refreshState);

  const getMonthlySchedule = async () => {
    const familyId = await AsyncStorage.getItem('familyId');

    console.log('캘린더 화면 월별 일정 불러오기');
    axiosInstance
      .get(`/calendar/month`, {
        params: {
          year: currentYear,
          month: currentMonth,
          familyId: familyId,
        },
      })
      .then(res => {
        console.log(res.data.data.calendarMonthScheduleResponseDtoList);
        setSchedules(res.data.data.calendarMonthScheduleResponseDtoList);
      })
      .catch(err => {
        console.log(err);
      });
  };

  useEffect(() => {
    getMonthlySchedule();
  }, [currentYear, currentMonth, refresh]);

  // 일정이 있는 경우 달력에 dot 표시
  const markedDates = schedules.reduce((acc, current) => {
    const start = new Date(current.startDate);
    const end = new Date(current.endDate);

    for (let date = start; date <= end; date = addDays(date, 1)) {
      const formattedDate = format(date, 'yyyy-MM-dd');
      acc[formattedDate] = {marked: true, dotColor: 'grey'};
    }

    return acc;
  }, {});

  const [selectedDate, setSelectedDate] = useState(
    format(new Date(), 'yyyy-MM-dd'),
  );

  const markedSelectedDates = {
    ...markedDates,
    [selectedDate]: {
      selected: true,
      marked: markedDates[selectedDate]?.marked,
    },
  };

  // 일정 미리보기 팝업 설정
  const [modalVisible, setModalVisible] = useState(false);
  const onModal = () => {
    setModalVisible(true);
  };

  return (
    <View style={styles.container}>
      <View style={styles.dateContainer}>
        {/* 중앙 정렬을 위해 안보이게 처리 */}
        <View style={{paddingLeft: 15, opacity: 0}}>
          <Ionicons name="calendar-outline" size={30} color="black" />
        </View>
        {/* 선택된 날짜정보 */}
        <View style={styles.selectDate}>
          <View style={styles.selectYear}>
            <Text style={styles.selectYearFont}>{currentYear}</Text>
          </View>
          <View style={styles.selectMonth}>
            <View style={{opacity: 0}}>
              <Text style={styles.selectMonthUnitFont}>월</Text>
            </View>
            <View>
              <Text style={styles.selectMonthFont}>{currentMonth}</Text>
            </View>
            <View>
              <Text style={styles.selectMonthUnitFont}>월</Text>
            </View>
          </View>
        </View>
        {/* 날짜 선택창 여는 버튼 */}
        <View style={styles.selectDateBtn}>
          <TouchableOpacity onPress={showPickerModal}>
            <Ionicons name="calendar-outline" size={30} color="black" />
          </TouchableOpacity>
        </View>
        {/* 연월 선택 모달 */}
        <Modal visible={isModalVisible} animationType="slide">
          <View style={styles.pickerContainer}>
            <DatePicker
              mode="monthYear"
              selectorStartingYear={2020}
              onMonthYearChange={selectedDate => {
                const [year, month] = selectedDate.split(' ');
                handleDatePickerChange(year, month);
              }}
            />
          </View>
        </Modal>
      </View>
      <View style={styles.calendarContainer}>
        <Calendar
          style={styles.calendar}
          markedDates={markedSelectedDates}
          // 달력 헤더 안보이게
          renderHeader={() => <></>}
          renderArrow={() => <></>}
          // 달력 타이틀 커스텀 => 2023 10
          monthFormat={`${currentYear}년 ${currentMonth}월`}
          // 스와이프로 월 변경 가능
          enableSwipeMonths={true}
          theme={customTheme}
          onDayPress={day => {
            setSelectedDate(day.dateString);
            onModal();
          }}
          onMonthChange={month => {
            setCalendarDate(month.dateString);
            setCurrentYear(month.year);
            setCurrentMonth(month.month);
          }}
          current={calendarDate}
          key={calendarDate}
        />
      </View>
      <SchedulePreview
        navigation={navigation}
        selectedDate={selectedDate}
        modalVisible={modalVisible}
        setModalVisible={setModalVisible}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    // justifyContent: 'center',
  },
  dateContainer: {
    marginTop: 40,
    flexDirection: 'row',
  },
  selectDate: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  selectDateBtn: {
    paddingLeft: 15,
    alignItems: 'center',
    justifyContent: 'center',
  },
  calendarContainer: {
    marginTop: 20,
    width: '87%',
    height: '60%',
  },
  calendar: {
    borderBottonWidth: 1,
    borderBottonColor: '#e0e0e0',
    width: '100%',
    height: '100%',
  },
  pickerContainer: {},
  selectYearFont: {
    fontSize: 24,
    fontFamily: 'Jost-Bold',
  },
  selectMonthFont: {
    fontSize: 40,
    fontFamily: 'Jost-SemiBold',
  },
  selectMonthUnitFont: {
    fontSize: 15,
    fontFamily: 'Pretendard-Medium',
  },
  selectMonth: {
    flexDirection: 'row',
    alignItems: 'baseline',
  },
});

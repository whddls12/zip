import {useRef, useEffect, useState} from 'react';
import {
  View,
  StyleSheet,
  Text,
  TextInput,
  Modal,
  Animated,
  TouchableOpacity,
  TouchableWithoutFeedback,
  Dimensions,
  PanResponder,
  Alert,
} from 'react-native';
import {format} from 'date-fns';
import DatePicker from 'react-native-date-picker';
import AsyncStorage from '@react-native-async-storage/async-storage';
import axiosInstance from '../../util/Interceptor';
import refreshState from '../../atoms/refreshState';
import {useRecoilState} from 'recoil';

export default function ScheduleUpdate(props) {
  const {schedule, scheduleId} = props;
  console.log('일정 데이터: ', schedule);
  console.log('일정 시작일: ', new Date(schedule.startDate));
  // 일정 수정에 필요한 데이터
  const [scheduleTitle, setScheduleTitle] = useState(''); // 제목
  const [startDate, setStartDate] = useState(new Date()); // 시작일
  const [endDate, setEndDate] = useState(new Date()); // 종료일

  const [refresh, setRefresh] = useRecoilState(refreshState);

  useEffect(() => {
    if (schedule) {
      setScheduleTitle(schedule.title);
      if (schedule.startDate) {
        setStartDate(new Date(schedule.startDate));
      }
      if (schedule.endDate) {
        setEndDate(new Date(schedule.endDate));
      }
    }
  }, [schedule]);

  // 일정 수정
  const updateSchedule = async () => {
    const familyId = await AsyncStorage.getItem('familyId');
    const scheduleStart = format(new Date(startDate), 'yyyy-MM-dd');
    const scheduleEnd = format(new Date(endDate), 'yyyy-MM-dd');
    // console.log('수정할 일정 Id: ', scheduleId);
    // console.log('가족 Id: ', familyId);
    // console.log('수정할 일정 제목: ', scheduleTitle);
    // console.log('일정 시작일: ', scheduleStart);
    // console.log('일정 종료일: ', scheduleEnd);

    axiosInstance
      .put(`/schedule/modify`, {
        scheduleId: scheduleId,
        familyId: familyId,
        scheduleTitle: scheduleTitle,
        startDate: scheduleStart,
        endDate: scheduleEnd,
      })
      .then(res => {
        console.log(res.data.msg);
        if (res.data.msg === '일정 수정 성공') {
          Alert.alert('', '일정이 수정되었습니다.', [
            {
              text: '확인',
              onPress: () => {
                setUpdateModalVisible(false);
                setRefresh(refresh => refresh * -1);
              },
            },
          ]);
        }
      })
      .catch(err => {
        console.log(err);
      });
  };

  const onDelete = async () => {
    const familyId = await AsyncStorage.getItem('familyId');
    // console.log('삭제할 일정 id:', schedule.scheduleId);
    // console.log('일정삭제 가족 id:', familyId);

    const scheduleDeleteRequestDto = {
      scheduleId: scheduleId,
      familyId: familyId,
    };

    axiosInstance
      .delete(`/schedule/delete`, {data: scheduleDeleteRequestDto})
      .then(res => {
        console.log(res.data.msg);
        if (res.data.msg === '일정 삭제 성공') {
          setRefresh(refresh => refresh * -1);
        }
      })
      .catch(err => {
        console.log(err);
      });
  };

  // 일정 삭제
  const deleteSchedule = async () => {
    Alert.alert(
      '',
      '정말로 삭제하시겠습니까?',
      [
        {text: '취소', onPress: () => {}, style: 'cancel'},
        {
          text: '삭제',
          onPress: () => {
            onDelete();
            Alert.alert('', '일정이 삭제되었습니다.');
          },
          style: 'destructive',
        },
      ],
      {
        cancelable: true,
        onDismiss: () => {},
      },
    );
  };

  // 시작일 종료일 모달 오픈 여부
  const [openPickStart, setOpenPickStart] = useState(false); // 시작일 모달 보여줄지 여부
  const [openPickEnd, setOpenPickEnd] = useState(false); // 종료일 모달 보여줄지 여부

  // 일정 등록창 모달 설정
  const {updateModalVisible, setUpdateModalVisible} = props;
  const screenHeight = Dimensions.get('screen').height;
  const panY = useRef(new Animated.Value(screenHeight)).current;
  const translateY = panY.interpolate({
    inputRange: [-1, 0, 1],
    outputRange: [0, 0, 1],
  });

  const resetScheduleUpdate = Animated.timing(panY, {
    toValue: 0,
    duration: 300,
    useNativeDriver: true,
  });

  const closeScheduleUpdate = Animated.timing(panY, {
    toValue: screenHeight,
    duration: 300,
    useNativeDriver: true,
  });

  const panResponders = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => false,
      onPanResponderMove: (event, gestureState) => {
        panY.setValue(gestureState.dy);
      },
      onPanResponderRelease: (event, gestureState) => {
        if (gestureState.dy > 0 && gestureState.vy > 1.5) {
          closeModal();
        } else {
          resetScheduleUpdate.start();
        }
      },
    }),
  ).current;

  useEffect(() => {
    if (props.updateModalVisible) {
      resetScheduleUpdate.start();
    } else {
      closeScheduleUpdate.start();
    }
  }, [props.updateModalVisible]);

  const closeModal = () => {
    closeScheduleUpdate.start(() => {
      setUpdateModalVisible(false);
    });
  };

  return (
    <Modal
      visible={updateModalVisible}
      animationType={'fade'}
      transparent
      statusBarTranslucent>
      <View style={styles.overlay}>
        <TouchableWithoutFeedback onPress={closeModal}>
          <View style={styles.background} />
        </TouchableWithoutFeedback>
        <Animated.View
          style={{
            ...styles.bottomSheetContainer,
            transform: [{translateY: translateY}],
          }}
          {...panResponders.panHandlers}>
          <View style={styles.createFormContainer}>
            {/* 취소 & 등록 버튼 */}
            <View style={styles.buttonContainer}>
              {/* 취소 버튼 */}
              <TouchableOpacity onPress={closeModal}>
                <Text style={styles.cancelButton}>취소</Text>
              </TouchableOpacity>
              {/* 등록 버튼 */}
              <TouchableOpacity onPress={updateSchedule}>
                <Text style={styles.writeButton}>완료</Text>
              </TouchableOpacity>
            </View>
            {/* 일정 이름 입력 */}
            <View style={styles.contentContainer}>
              <TextInput
                style={styles.titleInput}
                onChangeText={text => {
                  setScheduleTitle(text);
                }}
                value={scheduleTitle}
              />
            </View>
            {/* 일정 시작/종료 일자 선택 */}
            <View style={styles.selectDateContainer}>
              <View style={styles.selectDate}>
                <Text style={styles.selectDateLabel}>시작일</Text>
                <TouchableOpacity
                  style={styles.selectDateInput}
                  onPress={() => setOpenPickStart(true)}>
                  <Text style={styles.selectDateInputText}>
                    {format(new Date(startDate), 'yyyy.M.d')}
                  </Text>
                </TouchableOpacity>
                <DatePicker
                  modal
                  androidVariant="iosClone"
                  mode="date"
                  locale="ko-KR"
                  title={null}
                  confirmText="선택"
                  cancelText="취소"
                  open={openPickStart}
                  date={startDate}
                  onConfirm={date => {
                    setOpenPickStart(false);
                    setStartDate(date);
                  }}
                  onCancel={() => {
                    setOpenPickStart(false);
                  }}
                />
              </View>
              <View style={styles.selectDate}>
                <Text style={styles.selectDateLabel}>종료일</Text>
                <TouchableOpacity
                  style={styles.selectDateInput}
                  onPress={() => setOpenPickEnd(true)}>
                  <Text style={styles.selectDateInputText}>
                    {format(new Date(endDate), 'yyyy.M.d')}
                  </Text>
                </TouchableOpacity>
                <DatePicker
                  modal
                  androidVariant="iosClone"
                  mode="date"
                  locale="ko-KR"
                  title={null}
                  cancelText="취소"
                  confirmText="선택"
                  open={openPickEnd}
                  date={endDate}
                  onConfirm={date => {
                    setOpenPickEnd(false);
                    setEndDate(date);
                  }}
                  onCancel={() => {
                    setOpenPickEnd(false);
                  }}
                />
              </View>
            </View>
            <View style={{alignItems: 'center'}}>
              <TouchableOpacity
                style={styles.deleteButton}
                onPress={deleteSchedule}>
                <Text style={{color: 'white', fontSize: 15}}>삭제</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Animated.View>
        <TouchableWithoutFeedback onPress={closeModal}>
          <View style={styles.background} />
        </TouchableWithoutFeedback>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0, 0, 0, 0.4)',
  },
  background: {
    flex: 1,
  },
  bottomSheetContainer: {
    height: 400,
    backgroundColor: 'white',
    borderRadius: 20,
    marginHorizontal: 15,
    marginBottom: 15,
  },
  createFormContainer: {
    padding: 30,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 30,
  },
  titleInput: {
    marginTop: 30,
    marginBottom: 70,
    paddingHorizontal: 10,
    height: 50,
    fontSize: 20,
    borderBottomWidth: 1,
    borderColor: 'gray',
  },
  selectDate: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  selectDateInput: {
    marginBottom: 20,
  },
  selectDateInputText: {
    fontSize: 20,
    color: 'black',
    fontFamily: 'Jost-Medium',
  },
  selectDateLabel: {
    fontSize: 18,
  },

  // 버튼
  cancelButton: {
    fontSize: 20,
  },
  writeButton: {
    fontWeight: 'bold',
    fontSize: 20,
    color: 'black',
  },
  deleteButton: {
    backgroundColor: '#D93939',
    borderRadius: 12,

    width: 100,
    height: 33,
    alignItems: 'center',
    justifyContent: 'center',
  },
});

import {StyleSheet, View, Text, Image, TouchableOpacity} from 'react-native';
import axiosInstance from '../../util/Interceptor';
import {useEffect, useState} from 'react';
import format from 'date-fns/format';
import DiaryItemDetail from './DiaryItemDetail';
import {useIsFocused} from '@react-navigation/native';
import refreshState from '../../atoms/refreshState';
import {useRecoilState} from 'recoil';

export default function DiaryItem(props) {
  const {diarySummary, selectedYear, selectedMonth} = props;

  const [diary, setDiary] = useState([]);
  // console.log('일기 상세 데이터: ', diary);

  const [refresh, setRefresh] = useRecoilState(refreshState);

  // 일기 상세 데이터 가져오기
  const getDiaryDetail = () => {
    // console.log(
    //   '가져온 일기 데이터 : ',
    //   diarySummary,
    //   selectedMonth,
    //   selectedMonth,
    // );
    axiosInstance
      .get(`/diary/detail`, {
        params: {
          diaryId: diarySummary.diaryId,
        },
      })
      .then(res => {
        // console.log('일기 상세 데이터: ', res.data.data);
        setDiary(res.data.data);
      })
      .catch(err => {
        console.log(err);
      });
  };

  const isFocused = useIsFocused();

  useEffect(() => {
    getDiaryDetail();
  }, [isFocused, createModalVisible, refresh]);

  // 날짜 포맷팅
  const formatDay = createdAt => {
    // createdAt 형식 '2023-11-06'
    if (createdAt) {
      return createdAt.split('-')[2];
    }
  };

  // 일기 확장여부 (상세보기)
  const [expanded, setExpanded] = useState(false);

  const toggleExpanded = () => {
    setExpanded(!expanded);
  };

  // 일기 상세 모달 설정
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const onModal = () => {
    setCreateModalVisible(true);
  };

  return (
    <View>
      <TouchableOpacity
        style={[
          styles.eachDiary,
          styles.shadowProps,
          expanded ? styles.expandedItem : null,
        ]}
        onPress={() => onModal()}>
        {/* 일자 */}
        <View style={styles.diaryDay}>
          <Text style={styles.diaryDayFont}>{formatDay(diary.createdAt)}</Text>
          <Text style={styles.diaryDayUnitFont}>일</Text>
        </View>
        <View style={styles.diarySummary}>
          {/* 닉네임과 감정아이콘 */}
          <View style={styles.diaryAuthor}>
            <View>
              <Text style={styles.diaryNicknameFont}>{diary.name}</Text>
            </View>
            <View>
              {diary.emotionId === 1 ? (
                <Image
                  style={{width: 24, height: 24}}
                  source={require('../../assets/emotion/smile.png')}
                />
              ) : (
                <></>
              )}
              {diary.emotionId === 2 ? (
                <Image
                  style={{width: 24, height: 24}}
                  source={require('../../assets/emotion/wow.png')}
                />
              ) : (
                <></>
              )}
              {diary.emotionId === 3 ? (
                <Image
                  style={{width: 24, height: 24}}
                  source={require('../../assets/emotion/sad.png')}
                />
              ) : (
                <></>
              )}
              {diary.emotionId === 4 ? (
                <Image
                  style={{width: 24, height: 24}}
                  source={require('../../assets/emotion/angry.png')}
                />
              ) : (
                <></>
              )}
            </View>
          </View>
          {/* 제목 */}
          <View style={styles.diaryTitle}>
            <Text style={styles.diaryTitleFont}>{diary.title}</Text>
          </View>
        </View>
      </TouchableOpacity>
      <DiaryItemDetail
        diary={diary}
        selectedYear={selectedYear}
        selectedMonth={selectedMonth}
        createModalVisible={createModalVisible}
        setCreateModalVisible={setCreateModalVisible}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  eachDiary: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'white',
    borderRadius: 16,
    width: '100%',
    height: 80,

    padding: 20,
  },
  shadowProps: {
    // ios
    shadowColor: '#000000',
    shadowOpacity: 0.27,
    shadowRadius: 4.65,
    shadowOffset: {
      width: 0,
      height: 3,
    },
    // android
    elevation: 8,
  },
  // 일기 날짜
  diaryDay: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  diaryDayFont: {
    fontSize: 30,
    fontFamily: 'Jost-Bold',
  },
  diaryDayUnitFont: {
    fontSize: 15,
    fontFamily: 'Pretendard-Medium',
    alignSelf: 'flex-end',
    paddingBottom: 5,
  },
  // 일기 요약 (작성자, 감정아이콘, 제목)
  diarySummary: {
    marginLeft: 20,
  },
  // 일기 작성자
  diaryAuthor: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 5,
  },
  diaryNicknameFont: {
    fontSize: 18,
    fontFamily: 'Pretendard-SemiBold',
  },
  diaryTitleFont: {
    fontSize: 12,
    fontFamily: 'Pretendard-Medium',
  },
  diaryTitle: {
    paddingLeft: 5,
  },
});

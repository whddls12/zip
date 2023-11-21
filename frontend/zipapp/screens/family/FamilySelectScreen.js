import {
  View,
  Text,
  Button,
  FlatList,
  StyleSheet,
  Animated,
  TouchableOpacity,
} from 'react-native';
import React, {useState, useEffect, useRef} from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import axiosInstance from '../../util/Interceptor';
import Ionicons from 'react-native-vector-icons/Ionicons';
import {Alert} from 'react-native';

export default function FamilySelectScreen({navigation}) {
  const [familyList, setFamilyList] = useState(null);
  const rotateValue = useRef(new Animated.Value(0)).current; // 초기 값 0

  const rotateAnimation = rotateValue.interpolate({
    inputRange: [0, 1],
    outputRange: ['-15deg', '0deg'],
  });

  const navigateToFamilySelection = familyId => {
    console.log('선택한 가족 ID : ', familyId);
    AsyncStorage.setItem('familyId', JSON.stringify(familyId));
    navigation.navigate('홈');
  };

  const logout = async () => {
    // 알림 창을 표시
    Alert.alert(
      '로그아웃',
      '로그아웃 하시겠습니까?',
      [
        {
          text: '취소',
          style: 'cancel',
        },
        {
          text: '확인',
          onPress: async () => {
            // AsyncStorage를 사용하여 데이터를 클리어
            try {
              await AsyncStorage.clear();
              // 로그아웃 후 진입 화면으로 이동
              navigation.navigate('진입화면');
            } catch (error) {
              console.error('로그아웃 중 오류 발생:', error);
            }
          },
        },
      ],
      {cancelable: false},
    );
  };

  useEffect(() => {
    const animate = () => {
      // -15도에서 0도로
      Animated.timing(rotateValue, {
        toValue: 1,
        duration: 1000, // 1초
        useNativeDriver: true,
      }).start(() => {
        // 0도에서 -15도로
        Animated.timing(rotateValue, {
          toValue: 0,
          duration: 1000, // 1초
          useNativeDriver: true,
        }).start(animate); // 애니메이션 끝날 때마다 재시작
      });
    };

    animate(); // 애니메이션 시작

    return () => {
      rotateValue.stopAnimation(); // 컴포넌트 unmount 시 애니메이션 중지
    };
  }, []);

  useEffect(() => {
    async function fetchData() {
      axiosInstance.get(`/family/list`).then(response => {
        setFamilyList(response.data.data.familyListDetailResponseDtoList);
        console.log(response.data.data.familyListDetailResponseDtoList);
      });
    }

    fetchData();
  }, []);

  return (
    <View style={styles.container}>
      <Animated.Text
        style={{...styles.logo, transform: [{rotate: rotateAnimation}]}}>
        zip
      </Animated.Text>
      <TouchableOpacity
          style={headerStyles.logoutButton}
          onPress={() => {
            logout();
          }}>
          <Ionicons name="log-out-outline" size={30} color="black" />
        </TouchableOpacity>
      <View style={styles.conditionalContent}>
        <View
          style={[
            {
              alignSelf: 'stretch',
              alignSelf: 'flex-end',
              marginRight: 20,
              marginTop: 10,
            },
          ]}>
          <TouchableOpacity
            style={[
              familyList && familyList.length < 4
                ? {
                    backgroundColor: '#000', // 검은색 배경
                    paddingHorizontal: 20,
                    paddingVertical: 10,
                    borderRadius: 10,
                  }
                : {
                    backgroundColor: 'gray',
                    paddingHorizontal: 20,
                    paddingVertical: 10,
                    borderRadius: 10,
                  },
            ]}
            onPress={() => {
              navigation.navigate('가족코드 입력');
            }}
            disabled={familyList && familyList.length >= 4}
            >
            <Text style={[{color: '#FFF'}]}>초대코드 입력</Text>
          </TouchableOpacity>
        </View>
        {familyList && familyList.length > 0 ? (
          // familyList가 있는 경우
          <FlatList
            data={familyList}
            renderItem={({item}) => (
              <TouchableOpacity
                onPress={() => navigateToFamilySelection(item.id)}>
                <Text style={styles.familyText}>{item.name}</Text>
              </TouchableOpacity>
            )}
          />
        ) : (
          // familyList가 비어있는 경우
          <>
            <Text style={styles.familyText}>가족 만들기</Text>
          </>
        )}
        {familyList && familyList.length < 4 && (
          <TouchableOpacity onPress={() => navigation.navigate('가족추가')}>
            <Text style={styles.plusButton}>+</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    backgroundColor: 'white',
  },
  logo: {
    fontSize: 50,
    flex: 1,
    fontWeight: 'bold',
    position: 'absolute',
    top: 40,
    alignSelf: 'center',
    transform: [{rotate: '-15deg'}], // 이 부분을 추가합니다.
    color: 'black',
  },
  conditionalContent: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 150,
  },
  familyText: {
    fontSize: 30,
    fontWeight: 'bold',
    marginTop: 20,
    color: 'black',
  },
  plusButton: {
    fontSize: 30,
    marginTop: 20,
    color: 'gray',
  },
});

const headerStyles = StyleSheet.create({
  headerContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  logoutButton: {
    position: 'absolute',
    right: 0,
    marginTop: 70,
    marginRight: 20,
    bottom: 200
  },
});

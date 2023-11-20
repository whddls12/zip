import {createStackNavigator} from '@react-navigation/stack';
import HomeScreen from '../HomeScreen';
import IntroScreen from '../IntroScreen';
import LoginScreen from '../auth/LoginScreen';
import FamilySelectScreen from '../family/FamilySelectScreen';
import FamilyInsertScreen from '../family/FamilyInsertScreen';
import KakaoLoginScreen from '../auth/KakaoLoginScreen';
import KakaoLoginCallBack from '../auth/KakaoLoginCallBack';
import FamilyInvitedScreen from '../family/FamilyInvitedScreen';
import InputPhoneNumberScreen from '../family/InputPhoneNumberScreen';

const Stack = createStackNavigator();

export default function StackNavigator() {
  return (
    <Stack.Navigator initialRouteName="진입화면">
      <Stack.Screen
        name="홈"
        component={HomeScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="진입화면"
        component={IntroScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="로그인"
        component={LoginScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="가족선택"
        component={FamilySelectScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="가족추가"
        component={FamilyInsertScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="카카오"
        component={KakaoLoginScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="/auth/kakao/callback"
        component={KakaoLoginCallBack}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="가족코드 입력"
        component={FamilyInvitedScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="초대할 가족 폰 번호 입력"
        component={InputPhoneNumberScreen}
        options={{headerShown: false}}
      />
    </Stack.Navigator>
  );
}

import { ref } from 'vue';

export function useNotification() {
  const message = ref<string | null>(null);
  const type = ref<'success' | 'error' | 'info'>('info');

  function notify(msg: string, msgType: 'success' | 'error' | 'info' = 'info') {
    message.value = msg;
    type.value = msgType;
    setTimeout(() => {
      message.value = null;
    }, 4000);
  }

  function clear() {
    message.value = null;
  }

  return { message, type, notify, clear };
}

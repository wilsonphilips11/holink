import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ConfirmModal from '@/components/ConfirmModal.vue';

describe('ConfirmModal', () => {
  it('renders title and message when visible', () => {
    const wrapper = mount(ConfirmModal, {
      props: {
        visible: true,
        title: 'Are you sure?',
        message: 'This action cannot be undone.',
      },
    });

    expect(wrapper.text()).toContain('Are you sure?');
    expect(wrapper.text()).toContain('This action cannot be undone.');
  });

  it('does not render when not visible', () => {
    const wrapper = mount(ConfirmModal, {
      props: {
        visible: false,
        title: 'Are you sure?',
      },
    });

    expect(wrapper.find('.modal-overlay').exists()).toBe(false);
  });

  it('emits confirm on confirm click', async () => {
    const wrapper = mount(ConfirmModal, {
      props: { visible: true, title: 'Delete?' },
    });

    await wrapper.find('.btn-danger').trigger('click');
    expect(wrapper.emitted('confirm')).toBeTruthy();
  });
});

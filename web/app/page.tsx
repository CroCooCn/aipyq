import Link from 'next/link';

export default function Page() {
  return (
    <div>
      <h1>欢迎使用每日朋友圈助手</h1>
      <p>上传图片，生成多风格文案，选择模板，一键导出。</p>
      <ul>
        <li><Link href="/upload">开始创作</Link></li>
        <li><Link href="/history">历史与收藏</Link></li>
        <li><Link href="/account">账号与购买</Link></li>
      </ul>
    </div>
  );
}


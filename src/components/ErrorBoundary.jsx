import React from "react";

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ errorInfo });
    console.error("ErrorBoundary caught an uncaught error:", error, errorInfo);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null, errorInfo: null });
    if (this.props.onReset) {
      this.props.onReset();
    } else {
      window.location.reload();
    }
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }
      return (
        <div className="max-w-2xl mx-auto my-12 p-6 sm:p-8 rounded-3xl bg-base-100 border-2 border-error/30 shadow-xl text-center space-y-4">
          <div className="w-16 h-16 mx-auto rounded-2xl bg-error/10 text-error flex items-center justify-center text-3xl font-bold">
            ⚠️
          </div>
          <div className="space-y-1">
            <h2 className="text-xl font-black text-base-content">
              {this.props.title || "Đã xảy ra lỗi khi hiển thị nội dung này"}
            </h2>
            <p className="text-xs text-base-content/70">
              {this.state.error?.message || "Lỗi giao diện không mong muốn. Dữ liệu của bạn vẫn an toàn."}
            </p>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-2 pt-2">
            <button
              type="button"
              onClick={this.handleReset}
              className="btn btn-sm btn-primary text-white font-bold rounded-xl"
            >
              🔄 Thử tải lại trang
            </button>
            <button
              type="button"
              onClick={() => {
                window.location.href = "/";
              }}
              className="btn btn-sm btn-outline border-base-300 rounded-xl"
            >
              🏠 Về trang chủ
            </button>
          </div>

          {process.env.NODE_ENV !== "production" && this.state.error && (
            <details className="mt-4 text-left p-3 rounded-xl bg-base-200 text-xs font-mono text-error/80 overflow-auto max-h-48">
              <summary className="cursor-pointer font-bold mb-1">Chi tiết lỗi kỹ thuật (Dev)</summary>
              <pre className="whitespace-pre-wrap">{this.state.error.stack}</pre>
            </details>
          )}
        </div>
      );
    }

    return this.props.children;
  }
}
